FROM node:20
WORKDIR /app
COPY licencas-server/package*.json ./
RUN npm install
COPY licencas-server/ .
CMD ["node", "index.js"]