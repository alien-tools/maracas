# !/bin/sh

IMAGE=mrc-rest
REPOSITORY=mrc
AID=451421324356

docker build -t $IMAGE . &&
aws ecr get-login-password --region eu-west-3 | docker login --username AWS --password-stdin $AID.dkr.ecr.eu-west-3.amazonaws.com &&
#aws ecr create-repository --repository-name $REPOSITORY &&
docker tag mrc-rest:latest $AID.dkr.ecr.eu-west-3.amazonaws.com/$REPOSITORY &&
docker push $AID.dkr.ecr.eu-west-3.amazonaws.com/mrc

