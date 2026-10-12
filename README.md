<div align="center">

# 🛋️ جولة | jawlh

### كل المحلات، في مكان واحد.
**A shared furniture-store discovery, visit planning, and rating app.**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-19-149ECA?logo=react&logoColor=white)](https://react.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-Database-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Railway](https://img.shields.io/badge/Deploy-Railway-0B0D0E?logo=railway&logoColor=white)](https://railway.com/)

</div>

---

## 💡 About the Project | عن المشروع

**جولة** موقع عربي لمجموعة واحدة تخطط لزيارة محلات الأثاث. بدل ما تتوزع أسماء المعارض وروابط إنستقرام ومواقعها بين المحادثات، يجمعها الموقع في مكان واحد، مع خريطة تفاعلية وحالة زيارة مشتركة وتقييمات لكل عضو.

jawlh helps a small group organize furniture stores they want to visit, explore their locations on a map, track visits together, and share personal ratings.

## ✨ Features | المميزات

| الميزة | الوصف |
|---|---|
| 👥 الدخول بالاسم | كل عضو يكتب اسمه أول مرة، ويحفظ الموقع هوية الجهاز محليًا |
| 🏬 إدارة المحلات | إضافة وتعديل المحلات بواسطة أي عضو |
| 📍 خريطة تفاعلية | مشاهدة المحلات ذات الإحداثيات المحفوظة باستخدام Leaflet وOpenStreetMap |
| 🔎 بحث وفلاتر | البحث بالاسم والمنطقة والتصنيف، والفلترة حسب حالة الزيارة |
| 🕘 ترتيب المحلات | الأحدث إضافة يظهر أولًا |
| ✅ حالات الزيارة | لم تتم الزيارة، تمت الزيارة، مستبعد — حالة مشتركة للمجموعة |
| 🧾 سجل الحالة | عرض آخر شخص غيّر الحالة ووقت التغيير |
| ⭐ التقييمات | من نجمة إلى خمس نجوم، مع ملاحظة اختيارية لكل عضو |
| 🗑️ سلة المحذوفات | حذف ناعم (Soft Delete) وإمكانية الاسترجاع |
| 📱 واجهة عربية | اتجاه RTL، تصميم متجاوب بألوان البيج والأوف وايت |

**إضافة المحل حاليًا:** إدخال الاسم، رابط إنستقرام، المدينة، فئات الأثاث، رابط الخريطة، ورابط صورة اختياري. يمكن تحديد الموقع بالنقر على الخريطة داخل نموذج الإضافة.

## 🧠 Planned AI Feature | ميزة الذكاء الاصطناعي المخطط لها

The intended workflow is:

1. Paste a furniture store's Instagram profile URL.
2. Upload a screenshot of the profile or bio.
3. Analyze the screenshot with a vision-capable AI model to extract information *actually visible in the image*.
4. Review, correct, and complete any missing information.
5. Save the store.

> ⚠️ **Implemented:** Screenshot analysis via OpenAI Responses API. Upload a PNG/JPEG/WEBP profile screenshot in the store form and review proposed fields before saving. Instagram data is **not** automatically scraped. Automatic Google Maps URL coordinate extraction is also not implemented; the manual map picker is available.

## 🧱 Tech Stack | التقنيات

| Layer | Technology |
|---|---|
| Frontend | React 19, Vite, JavaScript, CSS |
| Maps | React Leaflet, Leaflet, OpenStreetMap |
| Backend | Java 21, Spring Boot 3.5, Spring Web |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Deployment | Railway + Docker |

## 📂 Project Structure

```text
jawlh/
├── frontend/
│   ├── src/
│   │   ├── main.jsx
│   │   └── style.css
│   ├── index.html
│   ├── server.mjs
│   ├── package.json
│   └── Dockerfile
├── backend/
│   ├── src/main/java/com/jawla/
│   │   ├── jawlhApplication.java
│   │   ├── api/
│   │   ├── config/
│   │   ├── model/
│   │   ├── repo/
│   │   └── service/
│   ├── src/main/resources/application.properties
│   ├── pom.xml
│   └── Dockerfile
└── README.md
```

## 🚀 Run Locally | التشغيل المحلي

**Requirements:** JDK 21, Maven, Node.js, npm, and a running MySQL server.

**1. Clone the repository**

```bash
git clone https://github.com/Abdulaziz-Shafae/jawlh.git
cd Jawla
```

**2. Configure and start the backend**

Create the MySQL database `jawla` or allow local creation through the JDBC URL configured in `application.properties`. Set credentials for your environment.

```bash
cd backend
mvn spring-boot:run
```

Default backend: `http://localhost:8080`.

Backend environment variables:

| Variable | Purpose |
|---|---|
| `DATABASE_URL` | Full JDBC URL, e.g. `jdbc:mysql://localhost:3306/jawla` |
| `DB_USER` | MySQL username |
| `DB_PASSWORD` | MySQL password |
| `FRONTEND_ORIGIN` | Allowed frontend origin, e.g. `http://localhost:5173` |
| `PORT` | HTTP port; defaults to `8080` |

**3. Start the frontend**

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**. The frontend uses `VITE_API_URL` when provided; otherwise it defaults to `http://localhost:8080`.

**Frontend:** https://jawlh.up.railway.app  
**Backend:** https://jawlh-back-production.up.railway.app

## ☁️ Deployment on Railway | النشر على Railway

Create **three services** in a single Railway project:

| Service | Source | Root Directory |
|---|---|---|
| MySQL | Railway database | Managed service |
| Backend | This GitHub repo | `/backend` |
| Frontend | This GitHub repo | `/frontend` |

Both application directories include their own Dockerfiles.

**Backend variables**

```env
DATABASE_URL=jdbc:mysql://<MYSQL_PRIVATE_HOST>:3306/<MYSQL_DATABASE>
DB_USER=<MYSQL_USER>
DB_PASSWORD=<MYSQL_PASSWORD>
FRONTEND_ORIGIN=https://jawlh.up.railway.app
```

Set the values using Railway's actual MySQL connection variables and service references, rather than copying the placeholders literally.

**Frontend variable**

```env
VITE_API_URL=https://jawlh-back-production.up.railway.app
```

Since Vite embeds `VITE_API_URL` at build time, the frontend must be **rebuilt/redeployed** after setting or changing this variable. Generate a public domain for both application services. The MySQL service does not require a public domain.

> Railway deployment is planned/configured but **not verified live** for this repository. Build, environment, network, and security checks are still required.

## 🔌 REST API

`POST /api/members` accepts a member name and returns a device token. Other endpoints require the `X-Member-Token` request header.

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/members` | Join with a name |
| GET | `/api/stores` | List active stores |
| POST | `/api/stores` | Add a store |
| PUT | `/api/stores/{id}` | Edit a store |
| PUT | `/api/stores/{id}/status` | Update visit status |
| PUT | `/api/stores/{id}/review` | Add/update your rating |
| DELETE | `/api/stores/{id}` | Move store to trash |
| GET | `/api/stores/trash` | List deleted stores |
| POST | `/api/stores/{id}/restore` | Restore a deleted store |

## 🎨 Design

- **Primary background:** `#F8F5F0` — Off-white
- **Secondary:** `#E8DDCE` — Light beige
- **Accent:** `#B69B7D` — Sand
- **Text:** `#39332E` — Dark brown
- **Direction:** Arabic-first / RTL
- **Approach:** Minimal, clean, mobile-friendly

## 🛠️ Development Status | حالة التطوير

**Core prototype available in the repository:** store CRUD, statuses, ratings, map display, local-name member entry, and soft deletion.

**Next steps:** 

- [x] Add screenshot upload and AI-based information extraction (requires OPENAI_API_KEY)
- [ ] Parse supported Google Maps links into coordinates
- [ ] Add proper private-group access protection
- [ ] Refactor backend into dedicated Controller / Service / DTO / Advice layers
- [ ] Improve validation, test coverage, error handling, and deployment readiness
- [ ] Verify end-to-end deployment on Railway

> 🔒 **Security notice:** This repository is public. A shared URL and name-only entry are not secure authentication. The current device-token approach does not restrict who may create a member. Do **not** use the prototype with sensitive or private data until group access control and production safeguards have been added. Never commit credentials or API keys.

---

<div align="center">

Made with ☕ for furniture-shopping adventures.

**جولة — كل المحلات، في مكان واحد.**

</div>

> The GitHub repository is still named `jawlh`. Application branding is `jawlh`.


### OpenAI screenshot analysis

Backend Railway variable: `OPENAI_API_KEY` (secret). Optional: `OPENAI_MODEL=gpt-4.1-mini`. Keep the key out of frontend variables and source code. Analysis endpoint: `POST /api/ai/instagram/analyze` using `multipart/form-data` fields `instagramUrl` and `image`, and header `X-Member-Token`. Images are analyzed only; they are not stored as store photos. API usage may incur costs. The current group access mechanism is not secure, so add authentication and rate limiting before sharing broadly.
