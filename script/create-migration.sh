#!/bin/bash
# create-migration.sh (feature 브랜치용)

if [ -z "$1" ]; then
    echo "사용법: ./scripts/create-migration.sh <설명>"
    echo "예시: ./scripts/create-migration.sh add_user_table"
    exit 1
fi

MIGRATION_DIR="src/main/resources/db/migration"
DESCRIPTION=$1

# 디렉토리 생성
mkdir -p "$MIGRATION_DIR"

# 최신 버전 찾기 (로컬 기준)
LATEST=$(ls -1 "$MIGRATION_DIR"/V*.sql 2>/dev/null | \
         sed 's/.*V\([0-9]*\)__.*/\1/' | \
         sort -n | \
         tail -1)

if [ -z "$LATEST" ]; then
    NEXT_VERSION=1
else
    NEXT_VERSION=$((LATEST + 1))
fi

# 날짜
DATE=$(date +%Y%m%d)

# Git 사용자명
AUTHOR=$(git config user.name | tr '[:upper:]' '[:lower:]' | tr ' ' '_')
if [ -z "$AUTHOR" ]; then
    AUTHOR="dev"
fi

# 파일명
FILENAME="V${NEXT_VERSION}__${DATE}_${AUTHOR}_${DESCRIPTION}.sql"
FILEPATH="${MIGRATION_DIR}/${FILENAME}"

# 이미 존재하는지 확인
if [ -f "$FILEPATH" ]; then
    echo "❌ 오류: 파일이 이미 존재합니다!"
    echo "   ${FILENAME}"
    exit 1
fi

# 템플릿 생성
cat > "$FILEPATH" << EOF
-- ============================================
-- Migration V${NEXT_VERSION}
-- ============================================
-- 설명: ${DESCRIPTION}
-- 작성자: ${AUTHOR}
-- 작성일: $(date +%Y-%m-%d)
-- 브랜치: $(git branch --show-current 2>/dev/null || echo "unknown")
-- ============================================

-- TODO: 아래에 SQL 작성

-- 예시:
-- CREATE TABLE example (
--     id VARCHAR(36) PRIMARY KEY COMMENT 'ID',
--     name VARCHAR(100) NOT NULL COMMENT '이름',
--     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='예시 테이블';

EOF

echo "✅ Migration 파일 생성 완료!"
echo ""
echo "📄 파일: ${FILENAME}"
echo "📊 버전: V${NEXT_VERSION}"
echo "👤 작성자: ${AUTHOR}"
echo "🌿 브랜치: $(git branch --show-current 2>/dev/null || echo "unknown")"
echo ""
echo "📝 다음 단계:"
echo "  1. 파일 열기: vi ${FILEPATH}"
echo "  2. SQL 작성"
echo "  3. Entity 수정"
echo "  4. 테스트: ./gradlew bootRun"
echo "  5. 커밋: git add . && git commit -m 'Add migration V${NEXT_VERSION}'"