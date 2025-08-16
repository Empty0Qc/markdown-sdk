#!/bin/bash

# Markdown SDK 发布脚本
# 支持选择发布范围、dry-run、禁止自动打 tag

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 默认配置
DRY_RUN=false
AUTO_TAG=true
PUBLISH_SCOPE="all"
VERSION=""
TAG_PREFIX="v"
AUTO_INCREMENT=false

# 可发布的模块列表
MODULES=(
    "markdown-common"
    "markdown-core"
    "markdown-engine"
    "markdown-render"
    "markdown-plugins"
    "markdown-plugins:table"
    "markdown-plugins:tasklist"
    "markdown-plugins:latex"
    "markdown-plugins:image-glide"
    "markdown-plugins:html"
    "markdown-debug"
    "markdown-benchmark"
)

# 帮助信息
show_help() {
    echo -e "${BLUE}Markdown SDK 发布脚本${NC}"
    echo ""
    echo "用法: $0 [选项] [版本号]"
    echo ""
    echo "选项:"
    echo "  -h, --help              显示帮助信息"
    echo "  -d, --dry-run           预览模式，不执行实际发布"
    echo "  -s, --scope SCOPE       发布范围: all(默认), core, plugins, MODULE_NAME"
    echo "  -t, --no-tag            禁止自动创建 Git tag"
    echo "  -p, --prefix PREFIX     Tag 前缀 (默认: v)"
    echo "  -i, --auto-increment    自动递增版本号 (兼容旧脚本)"
    echo ""
    echo "发布范围说明:"
    echo "  all                     发布所有模块"
    echo "  core                    仅发布核心模块 (common, core, engine, render)"
    echo "  plugins                 仅发布插件模块"
    echo "  MODULE_NAME             发布指定模块"
    echo ""
    echo "可用模块:"
    for module in "${MODULES[@]}"; do
        echo "  - $module"
    done
    echo ""
    echo "示例:"
    echo "  $0 1.0.0                          # 发布所有模块到版本 1.0.0"
    echo "  $0 --dry-run 1.0.0                # 预览发布 1.0.0"
    echo "  $0 --scope core 1.0.0             # 仅发布核心模块"
    echo "  $0 --scope markdown-engine 1.0.0  # 仅发布 engine 模块"
    echo "  $0 --no-tag 1.0.0                 # 发布但不创建 tag"
    echo "  $0 --auto-increment                # 自动递增版本号 (兼容模式)"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -s|--scope)
            PUBLISH_SCOPE="$2"
            shift 2
            ;;
        -t|--no-tag)
            AUTO_TAG=false
            shift
            ;;
        -p|--prefix)
            TAG_PREFIX="$2"
            shift 2
            ;;
        -i|--auto-increment)
            AUTO_INCREMENT=true
            shift
            ;;
        -*)
            echo -e "${RED}错误: 未知选项 $1${NC}"
            show_help
            exit 1
            ;;
        *)
            if [[ -z "$VERSION" ]]; then
                VERSION="$1"
            else
                echo -e "${RED}错误: 多余的参数 $1${NC}"
                show_help
                exit 1
            fi
            shift
            ;;
    esac
done

# 自动递增版本号 (兼容旧脚本)
auto_increment_version() {
    local VERSION_FILE="version.properties"
    
    # 如果没有 version.properties，先创建
    if [ ! -f "$VERSION_FILE" ]; then
        echo "versionCode=1" > $VERSION_FILE
        echo "versionName=1.0.0" >> $VERSION_FILE
    fi
    
    # 读取当前版本号
    local versionCode=$(grep "versionCode=" $VERSION_FILE | cut -d'=' -f2)
    local versionName=$(grep "versionName=" $VERSION_FILE | cut -d'=' -f2)
    
    # 自动递增 versionCode
    local newVersionCode=$((versionCode + 1))
    
    # 自动递增 versionName patch 号
    IFS='.' read -r major minor patch <<< "$versionName"
    patch=$((patch + 1))
    local newVersionName="${major}.${minor}.${patch}"
    
    # 更新 version.properties
    echo "versionCode=$newVersionCode" > $VERSION_FILE
    echo "versionName=$newVersionName" >> $VERSION_FILE
    
    VERSION="$newVersionName"
    echo -e "${GREEN}▶︎ 自动递增版本号: versionCode=${newVersionCode}, versionName=${newVersionName}${NC}"
}

# 处理版本号
if [[ "$AUTO_INCREMENT" == "true" ]]; then
    auto_increment_version
elif [[ -z "$VERSION" ]]; then
    echo -e "${RED}错误: 请提供版本号或使用 --auto-increment${NC}"
    show_help
    exit 1
fi

# 验证版本号格式 (语义化版本)
if ! [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9.-]+)?$ ]]; then
    echo -e "${RED}错误: 版本号格式不正确，请使用语义化版本 (如: 1.0.0, 1.0.0-alpha.1)${NC}"
    exit 1
fi

# 根据发布范围确定要发布的模块
get_modules_to_publish() {
    case "$PUBLISH_SCOPE" in
        "all")
            echo "${MODULES[@]}"
            ;;
        "core")
            echo "markdown-common markdown-core markdown-engine markdown-render"
            ;;
        "plugins")
            echo "markdown-plugins markdown-plugins:table markdown-plugins:tasklist markdown-plugins:latex markdown-plugins:image-glide markdown-plugins:html"
            ;;
        *)
            # 检查是否为有效模块名
            for module in "${MODULES[@]}"; do
                if [[ "$module" == "$PUBLISH_SCOPE" ]]; then
                    echo "$PUBLISH_SCOPE"
                    return
                fi
            done
            echo -e "${RED}错误: 无效的模块名 '$PUBLISH_SCOPE'${NC}"
            echo "可用模块: ${MODULES[*]}"
            exit 1
            ;;
    esac
}

# 检查工作区状态
check_git_status() {
    if [[ $(git status --porcelain) ]]; then
        echo -e "${RED}错误: 工作区有未提交的更改${NC}"
        echo "请先提交或暂存所有更改"
        exit 1
    fi
    
    if [[ $(git log --oneline origin/main..HEAD 2>/dev/null) ]]; then
        echo -e "${YELLOW}警告: 有未推送的提交${NC}"
        read -p "是否继续? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
}

# 检查 tag 是否已存在
check_tag_exists() {
    local tag="${TAG_PREFIX}${VERSION}"
    if git tag -l | grep -q "^${tag}$"; then
        echo -e "${RED}错误: Tag $tag 已存在${NC}"
        exit 1
    fi
}

# 执行发布
publish_modules() {
    local modules_to_publish=($(get_modules_to_publish))
    
    echo -e "${BLUE}准备发布以下模块到版本 $VERSION:${NC}"
    for module in "${modules_to_publish[@]}"; do
        echo "  - $module"
    done
    echo ""
    
    if [[ "$DRY_RUN" == "true" ]]; then
        echo -e "${YELLOW}[DRY RUN] 预览模式，不会执行实际操作${NC}"
    else
        read -p "确认发布? (y/N): " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            echo "发布已取消"
            exit 0
        fi
    fi
    
    echo -e "${GREEN}开始发布...${NC}"
    
    # 更新版本号文件
    echo -e "${BLUE}更新版本号到 $VERSION...${NC}"
    if [[ "$DRY_RUN" == "false" ]]; then
        echo "VERSION=$VERSION" > version.properties
    fi
    
    # 清理和测试
    echo -e "${BLUE}清理旧构建...${NC}"
    if [[ "$DRY_RUN" == "false" ]]; then
        ./gradlew clean
    fi
    
    echo -e "${BLUE}运行测试...${NC}"
    if [[ "$DRY_RUN" == "false" ]]; then
        ./gradlew test
    fi
    
    # 发布模块
    if [[ "$PUBLISH_SCOPE" == "all" ]]; then
        echo -e "${BLUE}发布所有模块...${NC}"
        if [[ "$DRY_RUN" == "false" ]]; then
            ./gradlew publishAllPublicationsToLocalRepoRepository
        fi
    else
        for module in "${modules_to_publish[@]}"; do
            echo -e "${BLUE}发布模块: $module${NC}"
            if [[ "$DRY_RUN" == "false" ]]; then
                ./gradlew :${module}:publishToMavenLocal
            fi
        done
    fi
    
    # 创建 Git tag
    if [[ "$AUTO_TAG" == "true" ]]; then
        local tag="${TAG_PREFIX}${VERSION}"
        echo -e "${BLUE}创建 Git tag: $tag${NC}"
        if [[ "$DRY_RUN" == "false" ]]; then
            git add .
            git commit -m "chore(release): v$VERSION" || true
            git tag -a "$tag" -m "Release version $VERSION"
            
            # 兼容旧脚本：自动推送 tag
            if [[ "$AUTO_INCREMENT" == "true" ]]; then
                git push origin "$tag"
                echo -e "${GREEN}Tag $tag 已推送到远程仓库${NC}"
            else
                echo -e "${GREEN}Tag $tag 已创建${NC}"
                echo -e "${YELLOW}提示: 使用 'git push origin $tag' 推送 tag 到远程仓库${NC}"
            fi
        fi
    fi
    
    echo -e "${GREEN}发布完成!${NC}"
    
    if [[ "$DRY_RUN" == "false" ]]; then
        echo ""
        echo "后续步骤:"
        echo "1. 推送代码: git push origin main"
        if [[ "$AUTO_TAG" == "true" && "$AUTO_INCREMENT" == "false" ]]; then
            echo "2. 推送标签: git push origin ${TAG_PREFIX}${VERSION}"
        fi
        echo "3. 在 GitHub 上创建 Release"
    fi
}

# 主函数
main() {
    echo -e "${BLUE}=== Markdown SDK 发布脚本 ===${NC}"
    echo "版本: $VERSION"
    echo "发布范围: $PUBLISH_SCOPE"
    echo "预览模式: $DRY_RUN"
    echo "自动标签: $AUTO_TAG"
    if [[ "$AUTO_INCREMENT" == "true" ]]; then
        echo "兼容模式: 自动递增版本号"
    fi
    echo ""
    
    if [[ "$DRY_RUN" == "false" ]]; then
        check_git_status
        if [[ "$AUTO_TAG" == "true" ]]; then
            check_tag_exists
        fi
    fi
    
    publish_modules
}

# 执行主函数
main