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
Where:  
- **α (text relevance weight)** → emphasizes keyword match.  
- **β (relevance score weight)** → emphasizes ML/curated score.  
- **γ (recency weight)** → boosts newer articles.  

---

### 6️⃣ Nearby News (Location-based)
**GET** `/api/v1/news/location`

**Params**
- `lat`, `lon` → user location.  
- `radiusKm` → search radius.  
- `limit` → default: `10`.  
- `cursor` → publication date (optional).  

**Logic**
- Uses MongoDB **geospatial `$nearSphere`** query on `location_point`.  
- Sorted by `publication_date DESC`.  

---

### 7️⃣ Trending Near Me
**GET** `/api/v1/news/trending`

**Params**
- `lat`, `lon` → user location.  
- `radiusKm` → default: `50`.  
- `limit` → default: `10`.  

**Logic**
- Uses **Redis trending grid system** (50×50 grid world map).  
- Scores based on:
  - Event volume (**views, clicks, shares**)  
  - Recency (**decay factor applied**)  
  - Distance (**geo boost → closer articles rank higher**)  

---

## 🧩 Trending Logic (50×50 Grid)

- World split into **50×50 grid cells**.  
- Each cell caches trending articles in **Redis (ZSET)**.  
- Events (**views, clicks, shares**) update **recency + volume scores** in real time.  
- **EventStreamingService** simulates continuous user activity for testing.  
- Queries snap user’s location to **nearest grid** and return **top trending articles**.  
- Results ranked by **final trending score**.  

---

## 📦 Metadata in Responses
Every API response includes:  
- `nextCursor` → for pagination.  
- `pageSize` → number of results returned.  
- `executedQuery` → exact MongoDB query executed (debugging / transparency).  

---

## 📌 Example Response (Query API)

```json
{
  "resultInfo": {
    "resultCodeId": "000",
    "resultMsg": "Query executed",
    "resultStatus": "SUCCESS"
  },
  "data": {
    "items": [
      {
        "id": "7a8d1946-0dd4-4aa0-873c-595e6be4602e",
        "title": "Pakistan grants temporary permission to Elon Musk's Starlink",
        "description": "Pakistan has temporarily approved Elon Musk's SpaceX to provide its satellite internet service Starlink in the country...",
        "url": "https://www.hindustantimes.com/world-news/pakistan-grants-temporary-permission-for-elon-musk-s-starlink-to-operate-in-country-101742611592143.html",
        "publicationDate": "2025-03-23T10:56:46",
        "category": ["world", "technology"],
        "relevanceScore": 0.69,
        "latitude": 17.866866,
        "longitude": 73.433543,
        "textScore": 1.1428571428571428
      },
      {
        "id": "af5bccc4-4e39-4af7-bbe3-cd2ef3356961",
        "title": "It is not an 'Elon Musk' device: Neuralink's 1st brain chip user",
        "description": "Noland Arbaugh, the first human to get Elon Musk's Neuralink brain chip implanted...",
        "url": "https://www.timesnownews.com/technology-science/how-elon-musks-mind-reading-chip-changed-this-mans-life-i-control-computers-with-thoughts-article-119381667/amp",
        "publicationDate": "2025-03-24T10:35:16",
        "category": ["startup", "technology"],
        "relevanceScore": 0.56,
        "latitude": 20.021137,
        "longitude": 73.437967,
        "textScore": 1.125
      }
    ],
    "metadata": {
      "pageSize": 10,
      "executedQuery": "And Filter{filters=[Text Filter{search='elon musk', textSearchOptions=...}, Document{{}}]}",
      "nextCursor": "2025-03-22T15:10:07"
    }
  }
}
