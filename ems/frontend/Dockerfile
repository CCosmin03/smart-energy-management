# Build
FROM node:20 AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

# Serve static cu nginx
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
