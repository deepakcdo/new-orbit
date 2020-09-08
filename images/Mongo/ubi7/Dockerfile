ARG LABEL

FROM registry.redhat.io/ubi7:${LABEL:-latest}
#RUN groupadd -r mongodb && useradd -r -g mongodb mongodb not needed yum creates user mongod
COPY mongodb-org-4.2.repo /etc/yum.repos.d
RUN yum install -y mongodb-org
#will replace local dir with persistent storage mount from kubenetes/openshift. configdb is only used in sharding
RUN  mkdir -p /data/db /data/log /data/configdb && chown -R mongod:mongod /data/db /data/log /data/configdb
EXPOSE 27017
#replace config from yum install with custom config
COPY mongod.conf /etc/mongod.conf
USER mongod:mongod
ENTRYPOINT ["mongod", "-f", "/etc/mongod.conf"]
