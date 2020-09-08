#!/usr/bin/env bash
#
# Sample usage:
#
#   HOST=localhost PORT=7000 ./test-em-all.bash
#
: ${CHAPTER_NUM=13}
SOURCE=/Users/deepakcdo/Documents/MySpace/Dev/Hands-On-Microservices-with-Spring-Boot-and-Spring-Cloud
DESTINATION=/Users/deepakcdo/Documents/MySpace/Dev/new-orbit/Apps/online-shop

function copy() {

  local SUB_FOLDER=$1
  echo "Processing  ${SUB_FOLDER}"

  local deleteCmd="rm -rf ${DESTINATION}/${SUB_FOLDER}/src"
  echo "Will run '${deleteCmd}'"
  local result=$(eval $deleteCmd)

  local copyCmd="cp -R ${SOURCE}/CHAPTER${CHAPTER_NUM}/${SUB_FOLDER}/src ${DESTINATION}/${SUB_FOLDER}"
  echo "Will run '${copyCmd}'"
  local result=$(eval $copyCmd)
  echo ""

  echo "Remove docker file"
  rm ${DESTINATION}/${SUB_FOLDER}/Dockerfile
  echo "Copying DockerFile"
  cp ${SOURCE}/CHAPTER${CHAPTER_NUM}/${SUB_FOLDER}/Dockerfile ${DESTINATION}/${SUB_FOLDER}/Dockerfile
}

copy api
copy util
copy microservices/product-composite-service
copy microservices/product-service
copy microservices/recommendation-service
copy microservices/review-service
copy spring-cloud/eureka-server
copy spring-cloud/gateway
copy spring-cloud/authorization-server
copy spring-cloud/config-server

echo "copy all yml files"
rm ${DESTINATION}/*.yml
cp -R ${SOURCE}/CHAPTER${CHAPTER_NUM}/*.yml ${DESTINATION}
rm ${DESTINATION}/config-repo/*.yml
cp -R ${SOURCE}/CHAPTER${CHAPTER_NUM}/config-repo/*.yml ${DESTINATION}/config-repo/
echo "copy test-em-all.bash"
cp -R ${SOURCE}/CHAPTER${CHAPTER_NUM}/test-em-all.bash ${DESTINATION}

#manually disabling header
echo ""
echo "Manually disabling header"
find . -name "*Tests.java" -exec sed -i  -e "s+\.expectHeader()\.contentType(APPLICATION_JSON)+//\.expectHeader()\.contentType(APPLICATION_JSON_UTF8)+g" {} +
find . -type f -name "*Tests.java-e" -exec rm {} \;

echo ""
echo "Fix test-em-all.bash scrip to use new path"
sed -i -e "s+\.components+\.details+g" test-em-all.bash
rm test-em-all.bash-e

echo "All Done !!!! "