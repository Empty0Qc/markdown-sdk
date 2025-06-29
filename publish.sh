#!/bin/bash

# 🟢 自动递增版本号 + Git Tag + 本地发布

GREEN='\033[0;32m'
NC='\033[0m'

VERSION_FILE="version.properties"

# 如果没有 version.properties，先创建
if [ ! -f "$VERSION_FILE" ]; then
  echo "versionCode=1" > $VERSION_FILE
  echo "versionName=1.0.0" >> $VERSION_FILE
fi

# 读取当前版本号
versionCode=$(grep "versionCode=" $VERSION_FILE | cut -d'=' -f2)
versionName=$(grep "versionName=" $VERSION_FILE | cut -d'=' -f2)

# 自动递增 versionCode
newVersionCode=$((versionCode + 1))

# 自动递增 versionName patch 号
IFS='.' read -r major minor patch <<< "$versionName"
patch=$((patch + 1))
newVersionName="${major}.${minor}.${patch}"

# 更新 version.properties
echo "versionCode=$newVersionCode" > $VERSION_FILE
echo "versionName=$newVersionName" >> $VERSION_FILE

# 输出新版本号
echo -e "${GREEN}▶︎ 新版本号: versionCode=${newVersionCode}, versionName=${newVersionName}${NC}"

# 提交 version.properties
git add $VERSION_FILE
git commit -m "chore(release): v$newVersionName"

# 创建 Git Tag
git tag "v$newVersionName"

# 推送 Tag
git push origin "v$newVersionName"

# Clean + Publish
echo -e "${GREEN}▶︎ 清理旧构建...${NC}"
./gradlew clean

echo -e "${GREEN}▶︎ 发布到本地仓库...${NC}"
./gradlew publishAllPublicationsToLocalRepoRepository

echo -e "${GREEN}🎉 发布完成！仓库在：$(pwd)/repo${NC}"

open $(pwd)/repo