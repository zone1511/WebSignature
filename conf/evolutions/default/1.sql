# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table signature_model (
  id                        bigint not null,
  hidden_markov_model       TEXT,
  average_training_score    double,
  mean                      TEXT,
  std                       TEXT,
  nb_features               integer,
  nb_states                 integer,
  nb_gaussians              integer,
  cretime                   timestamp not null,
  updtime                   timestamp not null,
  constraint pk_signature_model primary key (id))
;

create table user (
  id                        bigint not null,
  name                      varchar(255),
  model_id                  bigint,
  cretime                   timestamp not null,
  updtime                   timestamp not null,
  constraint pk_user primary key (id))
;

create sequence signature_model_seq;

create sequence user_seq;

alter table user add constraint fk_user_signatureModel_1 foreign key (model_id) references signature_model (id) on delete restrict on update restrict;
create index ix_user_signatureModel_1 on user (model_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists signature_model;

drop table if exists user;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists signature_model_seq;

drop sequence if exists user_seq;

