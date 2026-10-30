# ThreadKart

A small clothing store I built to learn Spring Boot properly: not just CRUD screens, but the boring-but-important parts too, like login, a cart that doesn't mix up users, stock that actually goes down when someone orders, and an admin who can ship or cancel orders.

**Live demo:** _add your Render link here_
(Free hosting sleeps after 15 min of no traffic, so the first load can take about a minute.)

Demo admin login: `admin@threadkart.in` / the password set in `ADMIN_PASSWORD`. You can also sign up as a normal customer.

## What it does

**Customers**
- Browse products, filter by category, search by name, sort by price, paginated results
- Pick a size and quantity, add to cart (works without logging in)
- Checkout with address validation (10 digit mobile, 6 digit pincode). Payment is Cash on Delivery only
- Free delivery above ₹999, otherwise ₹49
- "My orders" page with status, and orders can be cancelled until they ship

**Admin** (`/admin`)
- Dashboard with revenue, orders waiting to ship, and a low stock list
- Add / edit / delete categories and products, with image upload
- See every order, filter by status, move it through PLACED → SHIPPED → DELIVERED (or CANCELLED)

## Tech

Java 21, Spring Boot 3.4, Spring Security, Spring Data JPA (Hibernate), Thymeleaf, Bootstrap 5, MySQL. H2 is used for tests.

## A few decisions worth explaining

- **Cart lives in the session** (`@SessionScope` bean). My first version used a static list, which meant every user shared one cart. The session cart only stores product id, size and quantity. Prices are read from the database every time, so a price change by the admin shows up immediately.
- **Stock is checked again inside the order transaction.** Two people can have the last item in their cart at once; only the first checkout goes through. Cancelling an order puts the stock back.
- **Order items copy the product name and price.** If the admin edits or deletes a product later, old orders still show what the customer actually paid.
- **Images are stored in the database**, not in `src/main/resources/static`. Writing into the resources folder only works inside the IDE, and free hosts wipe the disk on every deploy.
- **Passwords are hashed with BCrypt**, admin pages need `ROLE_ADMIN`, and every form has CSRF protection. A customer opening someone else's order number gets a 404.
- Money uses `BigDecimal`, not `double`.

## Project structure

```
src/main/java/com/threadkart/store
├── config       security setup, startup data, shared model attributes
├── controller   home, shop, cart, checkout/orders, auth, admin
├── dto          form objects with validation rules
├── model        JPA entities
├── repository   Spring Data repositories
└── service      business logic (cart, orders, products, users)
src/main/resources
├── templates    Thymeleaf pages (fragments/ holds the shared navbar, footer etc.)
├── static       css, favicon
└── seed-images  pictures for the sample products
```

## Running it locally

You need JDK 21. Maven comes with the project (`mvnw`).

**Quickest way (no database setup):** uses an H2 file database in `./data`.

macOS / Linux / Git Bash:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Windows PowerShell (quote the `-D` property so PowerShell passes it as one Maven argument):
```powershell
.\mvnw.cmd "-Dspring-boot.run.profiles=h2" spring-boot:run
```

**With MySQL:** start MySQL; the `threadkart` database is created automatically. Default user/password is `root`/`root`. Change them with env vars if yours are different:

```bash
DB_USERNAME=root DB_PASSWORD=yourpassword ./mvnw spring-boot:run
```

On Windows use `mvnw.cmd` instead of `./mvnw`.

Open http://localhost:8086. On the first start the app creates the admin account and 9 sample products.

**Tests:** `./mvnw test` (runs against an in-memory H2 database)

## Environment variables

| Name | What it's for | Default |
|---|---|---|
| `DB_URL` | JDBC url of the MySQL database | local MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | database login | `root` / `root` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | admin account created on first start | `admin@threadkart.in` / `admin123` |
| `SEED_DEMO_DATA` | add sample products when the store is empty | `true` |
| `PORT` | HTTP port | `8086` |

## Deploying for free (Render + Aiven MySQL)

**Important:** Set the Render `DB_URL` environment variable in JDBC format: `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED`. Do not use Aiven's `mysql://` connection URI. Set `DB_USERNAME` and `DB_PASSWORD` separately. Render does not load `.env` files; configure these in the service's **Environment** settings. Render sets `PORT` automatically.

1. **Database:** create a free MySQL service on [Aiven](https://aiven.io/free-mysql-database). From its overview page copy the host, port, user and password. The JDBC url looks like
   `jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED`
2. **App:** on [Render](https://render.com), choose New → Web Service, connect this GitHub repo. Render finds the `Dockerfile` by itself. Choose the Free instance type.
3. Add the environment variables `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`.
4. Deploy. The first build takes a few minutes. You'll get a URL like `https://threadkart.onrender.com`.

## Things I'd add next

- Online payments with Razorpay test mode
- Order confirmation emails
- Product reviews and ratings
- A REST API for a mobile client
