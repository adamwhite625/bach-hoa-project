#!/bin/bash
cd admin-web && npm install && npm run build && cd .. && cp -r admin-web/dist dist
