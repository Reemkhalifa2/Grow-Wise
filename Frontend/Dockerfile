# Build Angular
FROM node:22 AS build

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

# Serve Angular
FROM nginx:alpine

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

RUN mv -f /usr/share/nginx/html/index.csr.html /usr/share/nginx/html/login.html

COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80