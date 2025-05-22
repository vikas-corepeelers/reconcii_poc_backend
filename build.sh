docker kill reconciliation
git pull origin main
mvn clean install
docker build --rm --no-cache --build-arg Port=8085 --tag reconciliation .
docker run -itd -v /home/sftp:/home/sftp -p 8085:8085 --rm --net cpl-net --env Port=8085 --env Profile=prod --name reconciliation reconciliation