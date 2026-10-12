# جولة | JAWLA

موقع عربي خاص بمجموعة واحدة لإدارة زيارات محلات الأثاث وتقييمها.

## التقنية

- Frontend: React + Vite + React Leaflet
- Backend: Java 21 + Spring Boot 3.5.6 + Spring Data JPA
- Database: MySQL
- Deployment: Railway (frontend / backend / MySQL)

## الميزات الأساسية

- دخول بالاسم وحفظ هوية الجهاز محليًا
- إضافة المحلات وتعديلها
- عرض المحلات كبطاقات أو خريطة
- البحث والفلترة، والترتيب من الأحدث إلى الأقدم
- تغيير حالة الزيارة مع اسم العضو وتاريخ التعديل
- تقييم مستقل لكل عضو من 1 إلى 5 نجوم وملاحظة اختيارية
- Soft Delete، سلة محذوفات واسترجاع
- اختيار إحداثيات المحل يدويًا على خريطة Leaflet
- حفظ روابط Instagram وGoogle Maps، وصورة برابط إن توفرت

## التشغيل المحلي

يتطلب Java 21 وMaven وNode.js وMySQL.

1. شغّل MySQL، وأنشئ قاعدة باسم `jawla` أو دع إعداد الاتصال ينشئها تلقائيًا.
2. داخل `backend`: شغّل `mvn spring-boot:run`. المتغيرات: `DATABASE_URL` (JDBC), `DB_USER`, `DB_PASSWORD`, `FRONTEND_ORIGIN`.
3. داخل `frontend`: شغّل `npm install` ثم `npm run dev`.
4. افتح `http://localhost:5173`.

## نشر Railway

1. اربط المستودع بخدمتين مستقلتين، واضبط Root Directory للخلفية `backend` وللواجهة `frontend`.
2. أضف خدمة MySQL.
3. في backend اضبط `DATABASE_URL=jdbc:mysql://<private-host>:3306/<database>` و`DB_USER` و`DB_PASSWORD` و`FRONTEND_ORIGIN=https://<frontend-domain>`.
4. في frontend اضبط `VITE_API_URL=https://<backend-domain>` كمتغير بناء، ثم أعد نشر الخدمة؛ لأن Vite يُضمّن القيمة وقت البناء.
5. لا تنسَ Generate Domain لكل من خدمتي الواجهة والباك إند.

## API

`POST /api/members` يدخل بالاسم ويصدر توكنًا خاصًا بالجهاز. بقية الطلبات تحتاج Header باسم `X-Member-Token`.

- `GET /api/stores`
- `GET /api/stores/trash`
- `POST /api/stores`
- `PUT /api/stores/{id}`
- `PUT /api/stores/{id}/status`
- `PUT /api/stores/{id}/review`
- `DELETE /api/stores/{id}`
- `POST /api/stores/{id}/restore`

## ما لم يكتمل

- **تحليل لقطة شاشة Instagram بالذكاء الاصطناعي ورفع الصور**: غير منفذ بعد، والإدخال اليدوي متاح.
- **فك روابط Google Maps تلقائيًا لاستخراج الإحداثيات**: غير منفذ؛ اختيار الموقع بالخريطة متاح.
- **حماية الوصول للمجموعة**: غير منفذة. رابط خاص وحده لا يحمي الخادم أو البيانات، والدخول بالاسم ليس مصادقة آمنة.
- **اختبارات البناء والتكامل**: لم يتم تشغيل اختبارات كاملة بعد.
- **هيكلة Backend**: النسخة الحالية مبدئية؛ يلزم نقل منطق JawlaController إلى Service وDtoIn/DtoOut وفق أسلوب المشروع قبل التوسع.

> مهم: لا تنشر هذه النسخة للاستخدام الفعلي ببيانات خاصة قبل إضافة حماية الوصول والتحقق من التشغيل.
