FROM node:24-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --include=dev --no-audit --no-fund && npm ls --depth=0
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
