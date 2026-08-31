#!/usr/bin/env bash
#
# 시연 계정 셋업 스크립트 (이슈 #171)
#
# 탁민주 5계정 + 송승윤 5계정을 실제 API 흐름 그대로 생성한다.
#
#   회원가입 → 마이데이터 연결 → 동기화 → (저금통) → 목표 생성 → 페이스메이커
#
# 마이데이터 연결이 프로필(이름/생년월일/소득)을 채우므로 목표 생성보다 먼저
# 와야 한다. 목표 실현가능성 판정이 소득을 참조하기 때문이다.
#
# 이 스크립트는 계정을 "방금 시작한 0% 상태"로만 만든다. 진행률·페이스·적립이력
# 주입은 이슈 #172에서 별도로 처리한다.
#
# 사용법:
#   BASE_URL=http://localhost:8080 ./scripts/setup-demo-accounts.sh
#
# 선행 조건:
#   - 목서버가 떠 있고 페르소나 시드(kb_user 10012~10021)가 적재돼 있을 것
#   - 백엔드가 떠 있을 것
#
set -uo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PASSWORD="${DEMO_PASSWORD:-demo1234!}"

# 목서버 시드의 kb_user 이메일과 반드시 일치해야 한다.
# 마이데이터 연결이 이 이메일로 목서버 사용자를 찾는다.
MINJOO_EMAILS=(minjoo1@naver.com minjoo2@naver.com minjoo3@naver.com minjoo4@naver.com minjoo5@naver.com)
LSSYL_EMAILS=(lssyl1@naver.com lssyl2@naver.com lssyl3@naver.com lssyl4@naver.com lssyl5@naver.com)

# --- 페르소나 파라미터 -------------------------------------------------------
# 탁민주: 전세 자기부담금. 목표 자산은 기존 저축계좌(SAVINGS)라 저금통을 만들지
#         않고, 미래로 자동이체도 걸지 않는다(은행 자동이체가 이미 있는 설정).
MINJOO_GOAL_TYPE="INDEPENDENCE"
# JSON 유니코드 이스케이프를 사용해 Windows Git Bash의 코드페이지 변환을 피한다.
MINJOO_GOAL_NAME="\\uC804\\uC138 \\uBCF4\\uC99D\\uAE08 \\uB9C8\\uB828"
MINJOO_GOAL_AMOUNT=20000000
MINJOO_GOAL_MONTHS=24

# 송승윤: 비상금. 목표 자산은 새로 만드는 저금통(MONEY_BOX)이고 자동이체를 건다.
LSSYL_GOAL_TYPE="EMERGENCY"
# JSON 유니코드 이스케이프를 사용해 Windows Git Bash의 코드페이지 변환을 피한다.
LSSYL_GOAL_NAME="\\uBE44\\uC0C1\\uAE08 \\uB9CC\\uB4E4\\uAE30"
LSSYL_GOAL_AMOUNT=5000000
LSSYL_GOAL_MONTHS=6
LSSYL_TRANSFER_AMOUNT=833334   # 5,000,000 / 6개월
LSSYL_TRANSFER_DAY=25          # 급여일과 동일

PACEMAKER_MAX_AMOUNT=30000

# 실패 여부를 파일로 남긴다. token=$(login ...) 처럼 명령 치환으로 호출되는
# 함수는 서브셸에서 돌아 변수 대입이 부모 셸에 전달되지 않기 때문이다.
#
# mktemp는 -t 옵션의 template 규칙이 GNU(XXXXXX 필수)와 BSD에서 달라
# 환경에 따라 실패한다. 경로를 직접 만들어 구현 차이를 피한다.
FAIL_FLAG="${TMPDIR:-/tmp}/miraero-demo-setup-fail.$$"
rm -f "$FAIL_FLAG"
trap 'rm -f "$FAIL_FLAG"' EXIT

# --- 유틸 -------------------------------------------------------------------

log()  { printf '  %s\n' "$*"; }
fail() { printf '  [실패] %s\n' "$*" >&2; : > "$FAIL_FLAG" 2>/dev/null || true; }

# jq 없이 동작하도록 최소한의 JSON 스칼라 추출만 한다.
json_get() {
  local key="$1" body="$2"
  printf '%s' "$body" \
    | tr -d '\n' \
    | sed -n "s/.*\"${key}\"[[:space:]]*:[[:space:]]*\"\{0,1\}\([^,\"}]*\)\"\{0,1\}.*/\1/p" \
    | head -1
}

# API 호출 후 성공 여부를 확인한다. 실패 시 응답 본문을 그대로 보여준다.
# 사용: call <METHOD> <PATH> <BODY|""> [AUTH_TOKEN]
call() {
  local method="$1" path="$2" body="${3:-}" token="${4:-}"
  local args=(-sS -X "$method" "${BASE_URL}${path}" -H 'Content-Type: application/json')
  [[ -n "$token" ]] && args+=(-H "Authorization: Bearer ${token}")
  [[ -n "$body"  ]] && args+=(-d "$body")
  curl "${args[@]}" 2>&1
}

is_success() { [[ "$(json_get success "$1")" == "true" ]]; }

# --- 단계별 함수 -------------------------------------------------------------

# 이미 가입된 계정이면 회원가입을 건너뛴다 (재실행 가능하게).
signup() {
  local email="$1" res code
  res=$(call POST /api/auth/signup "{\"email\":\"${email}\",\"password\":\"${PASSWORD}\"}")
  if is_success "$res"; then
    log "회원가입 완료"
    return
  fi
  code=$(json_get code "$res")
  # 응답이 JSON이 아니면(예: 톰캣 404 HTML) 서버 설정 문제이므로 즉시 실패시킨다
  if [[ -z "$code" ]]; then
    fail "회원가입 응답이 JSON이 아니다. 백엔드가 정상 기동했는지 확인할 것: ${res:0:120}"
    return 1
  fi
  log "회원가입 건너뜀 (이미 존재: ${code})"
}

login() {
  local email="$1" res
  res=$(call POST /api/auth/login "{\"email\":\"${email}\",\"password\":\"${PASSWORD}\"}")
  if ! is_success "$res"; then
    fail "로그인 실패: $res"
    return 1
  fi
  json_get accessToken "$res"
}

connect_and_sync() {
  local token="$1" res
  res=$(call POST /api/mydata/connect "" "$token")
  if ! is_success "$res"; then
    fail "마이데이터 연결 실패: $res"
    return 1
  fi
  res=$(call POST /api/mydata/sync "" "$token")
  if ! is_success "$res"; then
    fail "마이데이터 동기화 실패: $res"
    return 1
  fi
  log "마이데이터 연결·동기화 완료"
}

# accountType으로 계좌 하나의 ID를 뽑는다. 여러 개면 첫 번째를 쓴다.
find_account_id() {
  local token="$1" type="$2" res
  res=$(call GET "/api/accounts?accountType=${type}" "" "$token")
  printf '%s' "$res" | tr -d '\n' \
    | sed -n 's/.*"accounts"[[:space:]]*:[[:space:]]*\[[[:space:]]*{[[:space:]]*"accountId"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p' \
    | head -1
}

create_money_box() {
  local token="$1" account_id="$2" res
  res=$(call POST /api/money-boxes \
    "{\"accountId\":${account_id},\"moneyBoxType\":\"GOAL\",\"autoTransfer\":{\"amount\":${LSSYL_TRANSFER_AMOUNT},\"transferDay\":${LSSYL_TRANSFER_DAY}}}" \
    "$token")
  if ! is_success "$res"; then
    fail "저금통 생성 실패: $res"
    return 1
  fi
  json_get moneyBoxId "$res"
}

# 이미 목표가 있으면 그 goalId를 반환한다.
# 목표·저금통에는 "사용자당 1개" 제약이 없어서, 확인 없이 만들면 재실행할 때마다
# 계속 쌓인다.
find_existing_goal_id() {
  local token="$1" res
  res=$(call GET /api/goals "" "$token")
  printf '%s' "$res" | tr -d '\n' \
    | sed -n 's/.*"goalId"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p' | head -1
}

create_goal() {
  local token="$1" body="$2" res existing
  existing=$(find_existing_goal_id "$token")
  if [[ -n "$existing" ]]; then
    printf '%s' "$existing"
    return
  fi
  res=$(call POST /api/goals "$body" "$token")
  if ! is_success "$res"; then
    fail "목표 생성 실패: $res"
    return 1
  fi
  json_get goalId "$res"
}

create_pacemaker() {
  local token="$1" res
  res=$(call POST /api/pace-maker "{\"maxAmount\":${PACEMAKER_MAX_AMOUNT}}" "$token")
  if is_success "$res"; then
    log "페이스메이커 개설 완료"
  else
    # 사용자당 1개 제약이라 재실행 시 409가 정상이다.
    log "페이스메이커 건너뜀 ($(json_get code "$res"))"
  fi
}

# --- 페르소나별 셋업 ---------------------------------------------------------

setup_minjoo() {
  local email="$1" token account_id goal_id

  printf '\n[탁민주] %s\n' "$email"
  signup "$email" || return
  token=$(login "$email") || return
  connect_and_sync "$token" || return

  # 목표 자산 = 기존 저축계좌. 이 계좌의 잔액이 곧 진행률이다.
  account_id=$(find_account_id "$token" SAVINGS)
  if [[ -z "$account_id" ]]; then
    fail "저축계좌를 찾을 수 없음 (목서버 시드 확인 필요)"
    return
  fi
  log "저축계좌 연결: accountId=${account_id}"

  goal_id=$(create_goal "$token" \
    "{\"goalType\":\"${MINJOO_GOAL_TYPE}\",\"goalName\":\"${MINJOO_GOAL_NAME}\",\"goalAmount\":${MINJOO_GOAL_AMOUNT},\"goalMonths\":${MINJOO_GOAL_MONTHS},\"startAmount\":0,\"assets\":[{\"assetType\":\"ACCOUNT\",\"assetId\":${account_id}}]}") || return
  log "목표 생성 완료: goalId=${goal_id}"

  create_pacemaker "$token"
}

setup_lssyl() {
  local email="$1" token account_id money_box_id goal_id

  printf '\n[송승윤] %s\n' "$email"
  signup "$email" || return
  token=$(login "$email") || return
  connect_and_sync "$token" || return

  # 목표가 이미 있으면 저금통도 만들어져 있으므로 통째로 건너뛴다.
  # 저금통에는 사용자당 개수 제약이 없어 확인 없이 만들면 재실행 시 계속 쌓인다.
  goal_id=$(find_existing_goal_id "$token")
  if [[ -n "$goal_id" ]]; then
    log "목표 건너뜀 (이미 존재: goalId=${goal_id})"
    create_pacemaker "$token"
    return
  fi

  # 저금통은 목표보다 먼저 만들어야 한다. 목표 생성 시 assets 검증이
  # money_box 존재 여부를 확인하기 때문이다.
  account_id=$(find_account_id "$token" CHECKING)
  if [[ -z "$account_id" ]]; then
    fail "입출금계좌를 찾을 수 없음 (목서버 시드 확인 필요)"
    return
  fi

  money_box_id=$(create_money_box "$token" "$account_id") || return
  log "저금통 생성 완료: moneyBoxId=${money_box_id} (자동이체 ${LSSYL_TRANSFER_AMOUNT}원/${LSSYL_TRANSFER_DAY}일)"

  goal_id=$(create_goal "$token" \
    "{\"goalType\":\"${LSSYL_GOAL_TYPE}\",\"goalName\":\"${LSSYL_GOAL_NAME}\",\"goalAmount\":${LSSYL_GOAL_AMOUNT},\"goalMonths\":${LSSYL_GOAL_MONTHS},\"startAmount\":0,\"assets\":[{\"assetType\":\"MONEY_BOX\",\"assetId\":${money_box_id}}]}") || return
  log "목표 생성 완료: goalId=${goal_id}"

  create_pacemaker "$token"
}

# --- 실행 -------------------------------------------------------------------

printf '시연 계정 셋업 시작 (BASE_URL=%s)\n' "$BASE_URL"

for email in "${MINJOO_EMAILS[@]}"; do setup_minjoo "$email"; done
for email in "${LSSYL_EMAILS[@]}"; do setup_lssyl  "$email"; done

if [[ -e "$FAIL_FLAG" ]]; then
  printf '\n실패: 일부 단계가 실패했다. 위 로그를 확인할 것\n' >&2
  exit 1
fi
printf '\n완료: 10계정 셋업 성공\n'
