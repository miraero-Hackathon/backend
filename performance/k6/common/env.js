export function requireEnv(name) {
  const value = __ENV[name];

  if (!value) {
    throw new Error(`Required environment variable is missing: ${name}`);
  }

  return value;
}
