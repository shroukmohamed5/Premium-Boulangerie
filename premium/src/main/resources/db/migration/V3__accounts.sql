create table app_user (
  id             bigserial    primary key,
  email          varchar(254) not null unique,   -- stored lower-case
  name           varchar(80)  not null,
  password_hash  varchar(100) not null,          -- BCrypt
  avatar         text,                           -- small data: URL (128x128 jpeg), optional
  created_at     timestamptz  not null default now()
);

create table auth_session (
  token_hash  char(64)    primary key,           -- SHA-256 of the cookie value
  user_id     bigint      not null references app_user(id) on delete cascade,
  created_at  timestamptz not null default now(),
  expires_at  timestamptz not null
);
create index auth_session_user_idx on auth_session(user_id);
