DROP TABLE IF EXISTS products CASCADE;
CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  price DOUBLE,
  description TEXT,
  stock_quantity INT,
  image_url VARCHAR(1000),
  category VARCHAR(255)
);

INSERT INTO products (name, price, description, stock_quantity, image_url, category) VALUES
('Rainbow Origami Paper Set (200 sheets)', 299.00, 'Vibrant multi-colour origami paper in 20 colours. 15x15cm, perfect for beginners and advanced folders. Acid-free and fade-resistant.', 150, 'https://images.unsplash.com/photo-1612521564730-62fc7691cd85?w=400', 'Origami'),
('Metallic Origami Paper (100 sheets)', 399.00, 'Shimmering gold, silver and holographic sheets for stunning origami creations. 15x15cm. Ideal for festive décor.', 80, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400', 'Origami'),
('Giant Origami Paper A3 (50 sheets)', 349.00, 'Large-format 29x29cm origami paper in assorted pastel and bold colours. Great for oversized decorations and wall art.', 60, 'https://images.unsplash.com/photo-1557804506-669a67965ba0?w=400', 'Origami'),
('Origami Crane Kit with Instructions', 249.00, 'Everything you need to fold 100 paper cranes. Includes 100 patterned sheets and a step-by-step illustrated guide.', 120, 'https://images.unsplash.com/photo-1612521564730-62fc7691cd85?w=400', 'Origami'),
('Washi Origami Paper (60 sheets)', 449.00, 'Authentic Japanese washi-textured origami paper. Lightweight yet strong with traditional prints — florals, waves, and geometric patterns.', 75, 'https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400', 'Origami'),
('Premium Scrapbook Starter Kit', 599.00, 'Complete kit with 12x12 album, 50 patterned papers, 200+ stickers, 20 die-cut shapes, washi tape rolls, and adhesive. Everything to start your first scrapbook.', 50, 'https://images.unsplash.com/photo-1513475382585-d06e58bcb0e0?w=400', 'Scrapbooking');
