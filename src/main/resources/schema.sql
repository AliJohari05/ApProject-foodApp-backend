
-- === Users Table ===
CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       phone VARCHAR(15) UNIQUE NOT NULL,
                       email VARCHAR(100),
                       password VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL,  -- CUSTOMER, SELLER, DELIVERY, ADMIN
                       address TEXT,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- === Restaurants Table ===
CREATE TABLE restaurants (
                             id SERIAL PRIMARY KEY,
                             name VARCHAR(100) NOT NULL,
                             address TEXT NOT NULL,
                             phone VARCHAR(20) NOT NULL,
                             working_hours VARCHAR(100),
                             logo_url TEXT,
                             approved BOOLEAN DEFAULT FALSE,
                             owner_id INT NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (owner_id) REFERENCES users(id)
                                 ON DELETE CASCADE ON UPDATE CASCADE
);

-- === Categories Table ===
CREATE TABLE categories (
                            id SERIAL PRIMARY KEY,
                            title VARCHAR(100) NOT NULL,
                            description TEXT,
                            display_order INT DEFAULT 0,
                            icon VARCHAR(255),
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- === Menu Items Table ===
CREATE TABLE menu_items (
                            id SERIAL PRIMARY KEY,
                            restaurant_id INT NOT NULL,
                            category_id INT,
                            name VARCHAR(100) NOT NULL,
                            description TEXT,
                            price DECIMAL(10,2) NOT NULL,
                            image_url VARCHAR(255),
                            stock INT DEFAULT 0,
                            keywords TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
                                ON DELETE CASCADE ON UPDATE CASCADE,
                            FOREIGN KEY (category_id) REFERENCES categories(id)
                                ON DELETE SET NULL ON UPDATE CASCADE
);

-- === Orders Table ===
CREATE TABLE orders (
                        id SERIAL PRIMARY KEY,
                        customer_id INT NOT NULL,
                        restaurant_id INT NOT NULL,
                        status VARCHAR(20) DEFAULT 'PENDING',
                        total_price DECIMAL(10,2) DEFAULT 0.00,
                        delivery_address TEXT,
                        notes TEXT,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (customer_id) REFERENCES users(id)
                            ON DELETE CASCADE ON UPDATE CASCADE,
                        FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
                            ON DELETE CASCADE ON UPDATE CASCADE
);

-- === Order Items Table ===
CREATE TABLE order_items (
                             id SERIAL PRIMARY KEY,
                             order_id INT NOT NULL,
                             menu_item_id INT NOT NULL,
                             quantity INT NOT NULL,
                             price_at_order DECIMAL(10,2) NOT NULL,
                             FOREIGN KEY (order_id) REFERENCES orders(id)
                                 ON DELETE CASCADE,
                             FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
                                 ON DELETE CASCADE
);

-- === Deliveries Table (optional but recommended) ===
CREATE TABLE deliveries (
                            id SERIAL PRIMARY KEY,
                            order_id INT NOT NULL,
                            delivery_person_id INT NOT NULL,
                            status VARCHAR(20) DEFAULT 'ASSIGNED',
                            assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            delivered_at TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (order_id) REFERENCES orders(id)
                                ON DELETE CASCADE,
                            FOREIGN KEY (delivery_person_id) REFERENCES users(id)
                                ON DELETE SET NULL
);

-- === Trigger Function (shared for updated_at columns) ===
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- === Triggers for updated_at auto-update ===
CREATE TRIGGER set_updated_at_users
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_restaurants
    BEFORE UPDATE ON restaurants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_categories
    BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_menu_items
    BEFORE UPDATE ON menu_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_orders
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER set_updated_at_deliveries
    BEFORE UPDATE ON deliveries
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
