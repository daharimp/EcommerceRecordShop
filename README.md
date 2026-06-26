# Record Store E-Commerce API

A REST API for an online record store, built with Spring Boot and MySQL. It powers a
storefront where shoppers can browse vinyl and other products by category, filter by
price and subcategory, manage a shopping cart, save a profile, and check out. Admins
can manage the catalog. The provided front-end website talks to this API to do all of
the above.

## What it does

- **Browse & search products** — list everything, filter by category, price range, or subcategory, and look up a single product
- **Categories** — full management, with create/update/delete locked to admins
- **Shopping cart** — each logged-in user has their own persistent cart; add items (adding the same product again bumps the quantity instead of duplicating it), change quantities, or clear the whole thing
- **User profiles** — view and update your own contact and shipping details
- **Checkout** — turn your cart into an order in one request: it copies your shipping
  address from your profile, records what you paid, and empties your cart

## Built with

- **Java 17+** and **Spring Boot** (Spring Web, Spring Security)
- **JWT** authentication for login and role-based access
- **Spring Data JPA / Hibernate** for database access
- **MySQL** for storage
- **Maven** for builds
- **JUnit 5 + Mockito** for testing
- **Insomnia** for API testing

## Getting it running

1. **Set up the database.** Open the included SQL script in MySQL Workbench and run it.
   It creates the `recordshop` database with sample categories, products, and three demo
   users (`user`, `admin`, and `george` — all with the password `password`).

2. **Point the app at your database.** In `src/main/resources/application.properties`,
   set your MySQL connection. I keep my credentials in environment variables rather than
   committing them:

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/recordshop
   spring.datasource.username=${DB_USERNAME}
   spring.datasource.password=${DB_PASSWORD}
   ```

3. **Run it.** From the project root:

   ```bash
   ./mvnw spring-boot:run
   ```

   The API comes up at `http://localhost:8080`.

4. **Try it out.** Hit `GET http://localhost:8080/products` in a browser or Insomnia —
   no login needed. For anything that changes data, log in first at `POST /login` to get
   a token, then send it as a Bearer token.

## A quick tour of the endpoints

| Method | Endpoint | What it does | Who can use it |
|--------|----------|--------------|----------------|
| GET | `/products` | Search/list products (filters: `cat`, `minPrice`, `maxPrice`, `subCategory`) | Anyone |
| GET | `/products/{id}` | One product | Anyone |
| POST / PUT / DELETE | `/products` `/products/{id}` | Manage products | Admin |
| GET | `/categories` | All categories | Anyone |
| GET | `/categories/{id}/products` | Products in a category | Anyone |
| POST / PUT / DELETE | `/categories` `/categories/{id}` | Manage categories | Admin |
| GET | `/cart` | Your cart | Logged in |
| POST | `/cart/products/{id}` | Add a product (or +1 if already there) | Logged in |
| PUT | `/cart/products/{id}` | Set an item's quantity | Logged in |
| DELETE | `/cart` | Empty your cart | Logged in |
| GET / PUT | `/profile` | View / update your profile | Logged in |
| POST | `/orders` | Check out — turn your cart into an order | Logged in |

## Screenshots

> _Replace the lines below with real screenshots. Drop the image files into a `screenshots/`
> folder in the repo, then reference them here. To capture them: run the front-end website,
> and grab the home/catalog page, a filtered product view, the cart, and an Insomnia request
> showing a successful checkout response._

**Home / product catalog**
![Screenshot 2026-06-26 at 1.08.19 AM.png](screenshots/Screenshot%202026-06-26%20at%201.08.19%E2%80%AFAM.png)

**Filtering products**
![Screenshot 2026-06-26 at 1.05.53 AM.png](screenshots/Screenshot%202026-06-26%20at%201.05.53%E2%80%AFAM.png)

**Shopping cart**
[Screenshot 2026-06-26 at 12.29.52 AM.png](screenshots/Screenshot%202026-06-26%20at%2012.29.52%E2%80%AFAM.png)

**Checkout response in Insomnia**
![Screenshot 2026-06-26 at 1.14.09 AM.png](screenshots/Screenshot%202026-06-26%20at%201.14.09%E2%80%AFAM.png)

## One interesting piece of code

The part I find most interesting is how checkout records the price of each item. My first
instinct was to have an order just point at the products it contained and read their prices
when needed. But that's a trap: if an admin changes a product's price next week, every past
order would silently show the *new* price — so a customer's receipt would no longer match
what they actually paid.

So instead, at checkout I copy the product's current price into the order line item and store
it there permanently. The order keeps its own record, frozen at purchase time:

```java
for (ShoppingCartItem cartItem : cart.getItems().values())
{
    OrderLineItem line = new OrderLineItem();
    line.setOrderId(order.getOrderId());
    line.setProductId(cartItem.getProduct().getProductId());
    line.setSalesPrice(cartItem.getProduct().getPrice());   // freeze the price at purchase time
    line.setQuantity(cartItem.getQuantity());
    line.setDiscount(cartItem.getDiscountPercent());
    orderLineItemRepository.save(line);
}
```

The whole checkout method is also wrapped in `@Transactional`. Checkout does several things
in a row — saves the order, saves each line item, then clears the cart — and the transaction
makes that all-or-nothing. If any step fails, everything rolls back, so you can never end up
with an order that has no items, or a cart that got emptied without an order to show for it.
For something handling a purchase, that guarantee matters.

## Testing

Unit tests live under `src/test/java`. They use JUnit 5 with Mockito to test the service
logic in isolation — no database or Spring context needed, so they run in milliseconds.
The product search tests in particular pin down a bug I fixed, where search was accidentally
hiding every product that wasn't marked "featured."

```bash
./mvnw test
```
