# 📰 InBrief

A backend service built with **Spring Boot**, **MongoDB**, and **Redis** to fetch, search, and explore news articles.  
Supports **cursor-based pagination**, **trending feeds (grid-based caching)**, **geospatial queries**, and **text search with relevance ranking**.

---

## 🚀 Features
- Category & Source feeds with **cursor-based pagination**.  
- Search API using **MongoDB full-text search** with ranking (**text score + relevance**).  
- Relevance score feed with **duplicate-safe cursor pagination**.  
- Trending feed using **50×50 grid caching in Redis**.  
- Nearby news feed based on **user’s geolocation**.  
- Each response enriched with **metadata** (nextCursor, page size, executed query).  

---

## 📡 API Endpoints

### 1️⃣ Get by Category
**GET** `/api/v1/news/category`

**Params**
- `name` → category name (e.g., `"sports"`)  
- `limit` → default: `20`  
- `cursor` → publication date (optional)  

**Logic**
- Filters by category.  
- If cursor provided → fetch older than given date.  
- Sorted by `publication_date DESC`.  

---

### 2️⃣ Get by Source
**GET** `/api/v1/news/source`

**Params**
- `name` → source name (e.g., `"BBC News"`)  
- `limit` → default: `10`  
- `cursor` → publication date (optional)  

**Logic**
- Similar to category but filtered by `source_name`.  

---

### 3️⃣ Get by Relevance Score
**GET** `/api/v1/news/score`

**Params**
- `score` → threshold value.  
- `limit` → default: `10`.  
- `cursor` → pagination cursor (`lastScore__lastId`).  

**Logic**
- Filters articles with `relevance_score > threshold`.  
- Paginates using `(relevanceScore, id)` → ensures no duplicates when scores tie.  
- Sorted by `relevance_score DESC, id DESC`.  

---

### 4️⃣ Search Articles
**GET** `/api/v1/news/search`

**Params**
- `query` → keywords (full-text search).  
- `limit` → default: `10`.  
- `cursor` → publication date (optional).  

**Logic**
- Uses MongoDB `$text` search on `title + description`.  
- Returns `textScore` for relevance.  
- Sorted by `textScore DESC`.  
- Pagination via `publication_date`.  

---

### 5️⃣ Query News (Custom Orchestration)
**GET** `/api/v1/news/query`

**Params**
- `query` → custom orchestrated query.  
- `limit`, `cursor` → same as above.  

**Logic**
- Delegated to **QueryOrchestrationService**.  
- Supports **hybrid queries** combining:  
  - Full-text search (`textScore`).  
  - Relevance score (ML-based).  
  - Publication date.  
  - Geospatial filters.  

**Weight Logic**
- Final ranking is computed as a **weighted score**:  

