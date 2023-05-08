create table symbols
(
    id      integer generated always as identity
        constraint symbols_pk
            primary key,
    symbol  varchar(255) not null,
    region  varchar(2)   not null,
    updated date         not null,
    constraint symbol_region
        unique (symbol, region)
);

alter table symbols
    owner to postgres;

create table transactions
(
    id              integer generated always as identity,
    filername       varchar(255) not null,
    transactiontext varchar(255),
    moneytext       varchar(255),
    ownership       varchar(255),
    startdate       date         not null,
    value           double precision,
    filerrelation   varchar(255),
    shares          integer,
    filerurl        text,
    maxage          integer,
    symbol          varchar(255) not null,
    region          varchar(2)   not null,
    constraint transactions_symbols_symbol_region_fk
        foreign key (symbol, region) references symbols (symbol, region)
);

alter table transactions
    owner to postgres;