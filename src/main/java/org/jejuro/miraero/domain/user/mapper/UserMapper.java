package org.jejuro.miraero.domain.user.mapper;

import java.time.LocalDate;

import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.dto.response.ProfileResponse;

public interface UserMapper {

  //?대찓??以묐났 ?뺤씤
  boolean existsByEmail(@Param("email") String email);

  int save(User user);

  User findByEmail(@Param("email") String email);

  User findById(@Param("userId") Long userId);

  ProfileResponse findProfileById(@Param("userId") Long userId);
  int updatePasswordHash(
      @Param("userId") Long userId,
      @Param("passwordHash") String passwordHash
  );

  Long findMonthlyIncome(@Param("userId") Long userId);

  int updateKbPayId(@Param("userId") Long userId, @Param("kbPayId") Long kbPayId);

  // 마이데이터 연동 시 본인확인 정보로 회원가입 때의 목업 값을 덮어쓴다
  int updateProfile(
      @Param("userId") Long userId,
      @Param("name") String name,
      @Param("birthDate") LocalDate birthDate,
      @Param("companyName") String companyName,
      @Param("monthlyIncome") Long monthlyIncome
  );
}

