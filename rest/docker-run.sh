# !/bin/sh

docker build -t maracas .
docker run -p 8080:8080 --mount source=maracas-clones,target=/clones --mount source=maracas-reports,target=/reports maracas

