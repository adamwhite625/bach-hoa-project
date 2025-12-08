FROM node:22-slim

WORKDIR /app

COPY package*.json ./

RUN npm ci --only=production

COPY . .

EXPOSE 5000

RUN npm install -g pm2
CMD ["pm2-runtime", "server.js", "--name", "bach-hoa"]

