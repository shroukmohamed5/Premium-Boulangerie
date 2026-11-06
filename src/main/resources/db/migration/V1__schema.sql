create table menu_item (
  id           varchar(32)  primary key,
  cat          varchar(16)  not null check (cat in ('rolls','pastries','bread','coffee')),
  name         varchar(120) not null,
  price        numeric(8,2) not null check (price >= 0),
  tag          varchar(32)  not null,
  img          varchar(64)  not null,
  zoom         varchar(64),
  cup_from     varchar(16),
  cup_to       varchar(16),
  note         text         not null,
  ingredients  text         not null,
  allergens    text         not null,
  sort_order   int          not null,
  active       boolean      not null default true
);

create table deal (
  id           varchar(16)  primary key,
  name         varchar(120) not null,
  description  text         not null,
  was_price    numeric(8,2) not null,
  now_price    numeric(8,2) not null,
  img          varchar(64)  not null,
  zoom         varchar(64),
  cat          varchar(16)  not null,
  sort_order   int          not null,
  active       boolean      not null default true
);

-- price adjustments: coffee size / milk, custom box size / toppings
create table product_option (
  grp    varchar(16)  not null,
  label  varchar(64)  not null,
  delta  numeric(6,2) not null,
  primary key (grp, label)
);

create table promo_code (
  code         varchar(32) primary key,
  percent_off  int         not null check (percent_off between 1 and 100),
  active       boolean     not null default true
);

create table customer_order (
  id            bigserial    primary key,
  mode          varchar(16)  not null check (mode in ('delivery','pickup')),
  address       varchar(300),
  promo_code    varchar(32),
  subtotal      numeric(10,2) not null,
  discount      numeric(10,2) not null,
  delivery_fee  numeric(10,2) not null,
  total         numeric(10,2) not null,   -- computed by the server
  client_total  numeric(10,2),            -- what the page displayed
  mismatch      boolean      not null,    -- true when the two totals differ
  lang          varchar(5),
  status        varchar(24)  not null default 'received',
  created_at    timestamptz  not null default now()
);

create table order_item (
  id                bigserial    primary key,
  order_id          bigint       not null references customer_order(id) on delete cascade,
  item_key          varchar(300) not null,
  name              varchar(120),
  label             varchar(300),
  qty               int          not null check (qty > 0),
  unit_price        numeric(8,2) not null,  -- computed by the server
  client_unit_price numeric(8,2)
);
create index order_item_order_idx on order_item(order_id);

create table reservation (
  id          bigserial    primary key,
  name        varchar(100) not null,
  phone       varchar(40)  not null,
  res_date    date         not null,
  res_time    time         not null,
  guests      int          not null check (guests between 1 and 8),
  kind        varchar(16)  not null check (kind in ('Table','Box pick-up')),
  created_at  timestamptz  not null default now()
);
create index reservation_date_idx on reservation(res_date, res_time);

create table subscriber (
  email       varchar(254) primary key,
  created_at  timestamptz  not null default now()
);
