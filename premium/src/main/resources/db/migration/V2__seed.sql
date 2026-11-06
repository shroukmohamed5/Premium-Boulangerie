-- Menu and deals copied from the original index.html data
insert into menu_item(id,cat,name,price,tag,img,zoom,cup_from,cup_to,note,ingredients,allergens,sort_order) values
('cream','rolls','Vanilla Cream Swirl',6.5,'Signature','cream-pour',NULL,NULL,NULL,'A warm vanilla glaze poured over a golden, soft-centered roll.','Flour, butter, milk, cinnamon, vanilla cream','Gluten, dairy, eggs',1),
('choc','rolls','Chocolate Pour',7,'Warm','choc-pour',NULL,NULL,NULL,'Silky milk chocolate over cinnamon dough, glazed to order.','Flour, butter, milk chocolate, cinnamon','Gluten, dairy, eggs',2),
('pecan','rolls','Pecan Caramel Roll',7.5,'New','plated',NULL,NULL,NULL,'Toasted pecans, salted caramel and a touch of fresh mint.','Flour, butter, pecans, caramel, mint','Gluten, dairy, eggs, nuts',3),
('classic','rolls','Oven-Fresh Classic',5.5,'Daily','oven',NULL,NULL,NULL,'Our original cinnamon roll, straight from the oven.','Flour, butter, brown sugar, cinnamon','Gluten, dairy, eggs',4),
('croi','pastries','Butter Croissant',4,'Daily','pastry-croissant','cover 50% 72%',NULL,NULL,'Shattering layers, cultured butter, baked at dawn.','Flour, cultured butter, yeast','Gluten, dairy',5),
('pain','pastries','Pain au Chocolat',4.5,'Daily','pastry-pain',NULL,NULL,NULL,'Two bars of dark chocolate in laminated dough.','Flour, butter, dark chocolate','Gluten, dairy, soy',6),
('almond','pastries','Almond Pastry',4.8,'Favorite','counter','260% 90% 100%',NULL,NULL,'Frangipane, toasted almonds and powdered sugar.','Flour, butter, almonds, sugar','Gluten, dairy, eggs, nuts',7),
('cake','pastries','Strawberry Cream Slice',6.5,'Seasonal','cake-strawberry',NULL,NULL,NULL,'Vanilla sponge, whipped cream and fresh strawberries.','Flour, eggs, cream, strawberries','Gluten, dairy, eggs',8),
('sugar','pastries','Sugar Croissant',4.5,'New','pastry-sugar','cover 50% 40%',NULL,NULL,'Buttery croissants rolled in crystal sugar.','Flour, butter, sugar, yeast','Gluten, dairy',9),
('rais','pastries','Pain aux Raisins',4.6,'Daily','pastry-raisin',NULL,NULL,NULL,'Laminated dough, vanilla cream and plump raisins.','Flour, butter, raisins, cream','Gluten, dairy, eggs',10),
('pist','pastries','Pistachio Glaze Cake',7,'Seasonal','cake-pistachio','cover 50% 40%',NULL,NULL,'Soft sponge under a green pistachio glaze.','Flour, eggs, pistachio, butter','Gluten, dairy, eggs, nuts',11),
('bag','bread','Classic Baguette',3.5,'Daily','bread-baguette','cover 50% 18%',NULL,NULL,'A crisp crust and an airy, open crumb.','Flour, water, salt, yeast','Gluten',12),
('toast','bread','Brioche Toast Loaf',6,'Soft','bread-toast',NULL,NULL,NULL,'Sliced thick for toasting, golden and tender.','Flour, butter, eggs, milk','Gluten, dairy, eggs',13),
('wreath','bread','Festive Brioche Wreath',9.5,'Seasonal','bread-wreath',NULL,NULL,NULL,'A sweet brioche crown with a crunchy nut topping.','Flour, butter, eggs, hazelnut','Gluten, dairy, eggs, nuts',14),
('sour','bread','Sourdough Loaf',8.5,'Slow','bread-sourdough',NULL,NULL,NULL,'Wild-yeast starter, long ferment, crackling crust.','Flour, water, salt, starter','Gluten',15),
('chal','bread','Braided Challah',7.5,'Weekend','bread-challah',NULL,NULL,NULL,'A glossy, sesame-topped braid with a tender crumb.','Flour, eggs, honey, sesame','Gluten, eggs, sesame',16),
('esp','coffee','Espresso',3,'Classic','coffee-espresso','cover 50% 40%','#3b1f10','#7a4a2a','A double shot with thick crema and a caramel finish.','Single-origin espresso blend','None',17),
('ame','coffee','Americano',3.5,'Smooth','coffee-americano',NULL,'#2f1a0d','#6a3f22','Espresso stretched with hot water for a long, clean cup.','Espresso, hot water','None',18),
('flat','coffee','Flat White',4.2,'Best seller','coffee-flatwhite',NULL,'#8b5a35','#f3e3c8','Velvety microfoam over a double ristretto.','Espresso, steamed milk','Dairy',19),
('capp','coffee','Cappuccino',4.2,'Classic','coffee-cappuccino',NULL,'#9a6a40','#fbf1dc','Equal parts espresso, milk and airy foam, dusted with cocoa.','Espresso, steamed milk, foam','Dairy',20),
('lat','coffee','Café Latte',4.8,'Silky','coffee-latte',NULL,'#a86f3e','#e9c48c','A long, silky latte with a swirl of milk art.','Espresso, steamed milk','Dairy',21),
('mac','coffee','Iced Macchiato',5,'New','coffee-macchiato',NULL,'#a0693a','#f1dcbc','Espresso poured over ice and cold milk, layered by hand.','Espresso, milk, ice','Dairy',22),
('ice','coffee','Iced Latte',4.8,'Chilled','coffee-icedlatte',NULL,'#b07a4a','#f6e6cc','Cold milk and espresso swirled over ice.','Espresso, milk, ice','Dairy',23),
('cort','coffee','Cortado',3.8,'Short','coffee-cortado',NULL,'#6b3d20','#e3c08c','Espresso cut with warm milk, served in a small glass.','Espresso, steamed milk','Dairy',24),
('icc','coffee','Iced Cream Coffee',5.2,'Chilled','coffee-icedcream',NULL,'#a0693a','#f1dcbc','Cold coffee over ice with a ribbon of cream.','Coffee, cream, ice','Dairy',25),
('cold','coffee','Cold Brew',4.5,'Chilled','coffee-coldbrew',NULL,'#2a160b','#5a3320','Steeped for 18 hours, smooth and low in acidity.','Coffee, filtered water','None',26),
('hot','coffee','Hot Chocolate',4.5,'Cozy','coffee-hotchocolate',NULL,'#5a2e1a','#c58b5c','Dark chocolate melted into steamed milk, finished with cream.','Dark chocolate, milk, cream','Dairy',27);

insert into deal(id,name,description,was_price,now_price,img,zoom,cat,sort_order) values
('d1','Morning Pair','Vanilla Cream Swirl + Flat White',10.7,9.5,'cream-pour',NULL,'coffee',1),
('d2','Box of Six','Six rolls, any sauce',34,30,'choc-pour',NULL,'rolls',2),
('d3','Breakfast for Two','2 butter croissants + 2 flat whites',16.8,14.5,'pastry-croissant','cover 50% 72%','coffee',3),
('d4','Iced Duo','Two iced lattes to share',9.6,8,'coffee-icedlatte',NULL,'coffee',4);

insert into product_option(grp,label,delta) values
 ('size','Small',-0.50),('size','Medium',0),('size','Large',0.70),
 ('milk','Whole',0),('milk','Oat',0.60),('milk','Almond',0.60),
 ('box_size','4 rolls',24),('box_size','6 rolls',34),('box_size','12 rolls',62),
 ('topping','Pecans',1),('topping','Pearl sugar',1),('topping','Cinnamon sugar',1);

insert into promo_code(code,percent_off,active) values ('WELCOME10',10,true);
