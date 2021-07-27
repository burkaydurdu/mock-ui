from clojure:openjdk-13-lein-2.9.1

RUN apt-get update && apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_12.x | bash -
RUN apt-get update && apt-get install -y nodejs
RUN mkdir -p /src/

WORKDIR /src/
COPY project.clj package.json package-lock*.json /src/

RUN lein deps
COPY . /src/

RUN npm install
COPY . /src/

RUN npm install --silent
COPY . /src/
RUN lein less once
RUN npm run release
