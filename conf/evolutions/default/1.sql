# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table signature (
  id                        bigint not null,
  owner_id                  bigint,
  type                      integer,
  acquisition_method        integer,
  device_type               integer,
  device_name               varchar(255),
  device_details            varchar(255),
  device_height             integer,
  device_width              integer,
  samples                   TEXT,
  cretime                   timestamp not null,
  updtime                   timestamp not null,
  constraint ck_signature_type check (type in (0,1,2,3)),
  constraint ck_signature_acquisition_method check (acquisition_method in (0,1,2,3,4,5,6,7,8,9)),
  constraint ck_signature_device_type check (device_type in (0,1,2,3,4)),
  constraint pk_signature primary key (id))
;

create table signature_model (
  id                        bigint not null,
  owner_id                  bigint,
  hidden_markov_model       TEXT,
  gaussian_mixture_model    TEXT,
  average_training_score_local double precision,
  average_training_score_global double precision,
  mean_local                TEXT,
  std_local                 TEXT,
  mean_global               TEXT,
  std_global                TEXT,
  nb_features_local         integer,
  nb_states_local           integer,
  nb_gaussians_local        integer,
  nb_features_global        integer,
  nb_gaussians_global       integer,
  cretime                   timestamp not null,
  updtime                   timestamp not null,
  constraint pk_signature_model primary key (id))
;

create table user_table (
  id                        bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  model_id                  bigint,
  cretime                   timestamp not null,
  updtime                   timestamp not null,
  constraint pk_user primary key (id))
;

create sequence signature_seq;

create sequence signature_model_seq;

create sequence user_seq;

alter table signature add constraint fk_signature_owner_1 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_signature_owner_1 on signature (owner_id);
alter table signature_model add constraint fk_signature_model_owner_2 foreign key (owner_id) references user (id) on delete restrict on update restrict;
create index ix_signature_model_owner_2 on signature_model (owner_id);
alter table user add constraint fk_user_signatureModel_3 foreign key (model_id) references signature_model (id) on delete restrict on update restrict;
create index ix_user_signatureModel_3 on user (model_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists signature;

drop table if exists signature_model;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists signature_seq;

drop sequence if exists signature_model_seq;

drop sequence if exists user_seq;

