package com.example.xpenso.Transaction

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class ExpenseCategory(
    val icon: ImageVector,
    val color: Color,
    val keywords: List<String>
) {
    SHOPPING(
        Icons.Default.ShoppingBag,
        Color(0xFFF06292),
        listOf(
            "shopping",
            "shop",

            "jacket", "suit",
            "coat", "tie", "socks pack",
            "hoodie", "sweatshirt",
            // 👕 clothing
            "clothes", "clothing", "dress", "shirt", "tshirt", "t-shirt",
            "pant", "pants", "jeans", "trouser", "shorts",
            "kurta", "kurti", "saree", "lehenga", "salwar",
            "jacket", "coat", "hoodie", "sweater", "blazer",
            "night dress", "innerwear", "underwear", "bra", "vest",
            "mall", "shopping mall", "cloth",
            "fabric", "cloth shop", "boutique",
            "darji", "silai",
            "shopping",
            "shop",

            "gift", "present",
            "birthday gift",
            "anniversary gift",
            "wedding gift",
            "gift purchase",
            "gift item",
            "return gift",
            "surprise gift",

            "gold", "silver",
            "jewellery", "jewelry",
            "ring", "chain", "necklace",
            "bangle", "bracelet",
            "earring",
            "gold coin", "silver coin",
            "gold bar", "silver bar",
            "ornament",
            "hallmark",
            "24k", "22k",
            "tanishq", "malabar", "kalyan",
            "jewellers", "jewelers",
            "kid", "kids", "child", "children", "baby",
            "infant", "toddler",

            // 🧸 baby items
            "diaper", "nappy", "wipes",
            "baby food", "cerelac", "formula",
            "milk powder", "kids clothes", "kids shoes", "kids bag",
            "school uniform", "kids uniform", "kids stationery",
            "baby milk", "baby formula", "kids snack",

            "baby diaper", "baby powder", "baby toy",
            "kids toy", "kids book",
            // 🍼 care products
            "baby shampoo", "baby soap", "baby lotion",
            "baby oil", "powder", "cream",

            // 👶 clothes & needs
            "baby clothes", "kids dress",
            "frock", "romper", "onesie",

            // 🎒 school & toys
            "school bag", "lunch box",
            "toys", "toy",
            "crayons", "color pencils",
            "notebook", "books",


            // ✂️ tailoring
            "tailor", "tailoring", "stitch", "stitching",
            "alter", "alteration",
            "blouse stitching", "pant stitching",
            // 👟 footwear
            "shoes", "sneakers", "sandals", "slippers", "chappal",
            "heels", "boots", "loafers", "flip flop",

            // 👜 accessories
            "bag", "handbag", "wallet", "purse", "belt",
            "cap", "hat", "sunglasses", "spectacles",
            "watch", "smartwatch",

            // 🧣 fashion items
            "scarf", "shawl", "dupatta",
            "jewellery", "jewelry", "ring", "earring", "necklace",
            "bangle", "bracelet", "chain",

            // 🛒 online shopping platforms
            "amazon", "flipkart", "myntra", "ajio", "meesho",
            "snapdeal", "nykaa", "shopclues",
            // 👟 footwear
            "shoes", "sneakers", "sandals", "slippers", "chappal",
            "heels", "boots", "loafers", "flip flop", "Shoe",
            "socks", "sock", "ankle socks", "cotton socks", "woolen socks",
            // 🧾 general purchase words
            "purchase", "bought", "order", "ordered" ,
            "amazon order", "flipkart order", "myntra order",
            "ajio order", "meesho order", "nykaa order",
            "shopping mall", "mall shopping", "retail store",

    // Hair & grooming
    "haircut", "hair cut", "salon",
    "haircutting", "hair cutting", "trim",
    "beard trim", "saloon", "spa",
    "barber", "barbershop", "hairdressing",
    "hair styling", "hair treatment",
    "hair spa", "hair trim",
    "beard", "shave",

            "candle", "church candle",
            "prayer candle",
            "rosary",
            "cross pendant",
            "bible",
            "holy water",
            "church offering",
            "church donation",
            "christmas candle",
            "nativity set",

    // Bath & hygiene
    "soap", "handwash", "body wash",
    "loofah", "toothpaste", "toothpaste tube",
    "toothbrush", "tooth brush", "hairbrush",
    "hair brush", "beard brush",
    "mouthwash", "razor",
    "shaving cream", "aftershave",
    "comb", "brush",

    // Hair products
    "shampoo", "shampoo bottle",
    "conditioner", "conditioner bottle",
    "hair oil", "hair oil bottle",
    "hair gel", "hair cream",
    "hair serum", "hair spray",
    "hair wax", "hair color",
    "hair dye",

    // Skincare
    "face wash", "cleanser",
    "face cream", "beauty cream",
    "face pack", "face mask",
    "face toner", "face serum",
    "moisturizer", "lotion",
    "body lotion", "skin lotion",
    "sunscreen", "spf",

    // Makeup / cosmetics
    "cosmetic", "cosmetics",
    "makeup", "make up",
    "lipstick", "lip balm",
    "lip gloss", "lip tint",
    "lip liner",
    "foundation", "foundation cream",
    "compact", "compact powder",
    "concealer", "blush",
    "highlighter",
    "eyeliner", "kajal",
    "mascara",
    "eyeshadow", "eye shadow",
    "nail polish", "nail paint",

    // Fragrance
    "perfume", "fragrance",
    "deodorant","ittar" , "itra" , "Attar"
            , "hair serum", "wax", "gel",

    // Beauty services
    "facial", "grooming",
    "beauty", "skincare",
    "skin care", "makeover",

    // Small grooming tools
    "trimmer", "nail cutter"
        )
    ),

    BILLS(
        Icons.Default.RequestQuote,
        Color(0xFFFFB300),
        listOf(
            "bill", "bills",
            "electricity", "power bill",
            "water bill",
            "gas bill", "lpg",
            "wifi", "broadband",
            "recharge", "mobile recharge",
            "mobile bill", "postpaid",
            "dth", "cable tv",
            "internet", "electric bill", "light bill", "power",
            "eb bill", "electric board", "water tax",
            "gas cylinder", "broadband bill",
            "utility", "utilities" , "electricity bill", "light bill", "water charge",
            "wifi recharge", "internet recharge",
            "mobile recharge", "sim recharge",
            "postpaid bill", "broadband recharge",
            "salary", "driver salary", "driver payment",
            "driver wages", "mali salary", "gardener salary",
            "maid salary", "maid payment",
            "servant salary", "helper salary",
            "watchman salary", "security guard salary",
            "worker salary", "labour", "labor",
            "bank", "bank charge", "transaction fee",
            "atm charge", "atm fee",
            "transfer fee", "processing fee",
            "interest", "bank interest" , "Redeem code" ,
            "bank penalty", "late fee",
            "loan emi", "credit card bill",
            "credit card payment" ,
            "gas", "gas bill", "gas cylinder",
            "lpg", "lpg cylinder", "bharat gas",
            "indane", "hp gas", "gas refill",
            "cylinder refill", "lpg refill", "gas booking",
            "insurance", "policy", "premium",
            "health insurance", "bike insurance", "car insurance"
        )
    ),

    EDUCATION(
        Icons.Default.School,
        Color(0xFF3949AB),
        listOf(
            "school", "college", "fees", "tuition",
            "coaching", "course", "books", "university", "education",
            "exam fee", "admission", "semester fee",
            "lab fee", "library fee", "college fee",
            "school fee", "exam registration", "school admission", "college admission",
            "exam form", "study material", "online course",
            "coaching fee", "tuition fee", "study books" , "paper", "notebook", "copy", "copies", "copy book", "copy books",
            "register", "note book", "exercise book",
            "pen", "pencil", "eraser", "sharpener",
            "stationery", "stationary",
            "print", "printing", "printout",
            "xerox", "photocopy", "copy print",
            "spiral binding", "binding" , "spiral",
            "daycare", "playschool", "nursery",
            "school fees", "tuition",
            "udemy subscription", "coursera", "byju", "unacademy", "vedantu",
            "physics wallah"
        )

    ),
    ELECTRONICS(
        Icons.Default.Devices,
        Color(0xFF5E35B1),
        listOf(
            "electronics", "electronic",

            // 📺 home appliances
            "tv", "television", "smart tv", "android tv",
            "washing machine", "refrigerator", "fridge",
            "microwave", "oven", "microwave oven",
            "induction", "induction stove",
            "mixer", "grinder", "juicer" , "cctv", "cctv camera", "security camera",
            "ip camera", "surveillance camera",
            "dvr", "nvr",
            "camera installation", "cctv installation",
            "dishwasher", "chimney",
            "geyser", "water heater",
            "fan", "ceiling fan", "table fan",
            "heater", "room heater",
            "air cooler", "cooler",
            "ac", "air conditioner", "immersion rod", "electric heater", "room heater",
            "electric iron", "steam iron", "toaster",
            "electric kettle", "blender", "hand blender",

            "iphone", "android", "samsung",
            "realme", "oneplus", "xiaomi",
            "charger cable", "type c cable", "usb cable",

            // 📱 mobile & gadgets
            "mobile", "phone", "smartphone",
            "feature phone",
            "tablet", "ipad",
            "smartwatch", "watch",
            "fitness band", "band",
            "earphone", "earphones",
            "headphone", "headphones",
            "earbuds", "airpods",
            "speaker", "bluetooth speaker",

            // 💻 computer & accessories
            "laptop", "notebook", "macbook",
            "computer", "desktop", "pc",
            "monitor", "display",
            "keyboard", "mouse",
            "webcam", "microphone",
            "printer", "scanner",
            "ups", "inverter",
            "cpu", "cabinet",
            "graphics card", "gpu",
            "ram", "memory",
            "hard disk", "hdd",
            "ssd", "pendrive", "usb",
            "external hard disk",

            // 🔌 power & networking
            "charger", "fast charger",
            "adapter", "power adapter",
            "power bank",
            "extension board", "extension",
            "plug", "socket", "switch",
            "router", "wifi router",
            "modem", "wifi",
            "lan cable", "ethernet",

            // 🎮 entertainment
            "playstation", "ps4", "ps5",
            "xbox", "gaming console",
            "joystick", "controller",
            "vr headset",

            // 📷 camera & accessories
            "camera", "dslr",
            "mirrorless",
            "action camera", "gopro",
            "tripod", "gimbal",
            "memory card", "sd card",

            // 🧾 services & repairs
            "repair", "service",
            "screen replacement",
            "battery replacement",
            "led bulb",
            "bulb",
            "tube light",
            "led tube",
            "stabilizer",
            "power strip",
            "electric board",
            "led light" , "apple", "samsung", "realme",
            "redmi", "xiaomi", "oneplus", "techno" , "Xiaomi" , "Asus Laptop" , "macbook",  "samsung Laptop",
            "boat", "jbl", "sony", "boult" ,
            "hp laptop", "dell laptop", "lenovo laptop"


        )
    ),

    FOOD(
        Icons.Default.Restaurant,
        Color(0xFFF57C00),
        listOf(
            "food", "dinner", "lunch", "breakfast", "pizza", "biscuit", "snacks", "burger",
            "meal", "thali", "restaurant",
            "hotel food", "tea", "coffee",
            "chai", "cold drink", "juice", "samosa", "dosa", "idli",
            "vada", "biryani", "paneer",
            "roll", "sandwich", "pasta", "Mushroom",
            "cake", "pastry", "ice cream", "chocolate",
            "chips", "alcohol", "liquor",
            "beer", "beer bottle", "beer can",
            "whisky", "whiskey",
            "rum", "vodka", "gin", "tequila",
            "wine", "red wine", "white wine",
            "champagne", "bar", "bar bill", "cake" , "waffle",
            "liquor shop", "wine shop", "soft drink" , "drink",
            "namkeen packet", "jalebi" , "rissole" , "imarti",
            "street food", "dhaba food" , "water",
            "lunch box", "tiffin" , "dominos", "pizza hut", "kfc",
            "burger king", "subway", "mcdonalds", "naturals" , "ice cream",
            "haldiram", "bikanervala", "wow momo", "water ball" , "pani puri" ,
            "zomato", "swiggy", "food delivery",
            "tea stall", "chai stall", "dhaba" , // 🥤 Cold drinks / beverages
            "coke", "coca cola", "pepsi", "sprite", "fanta", "thums up", "limca",
            "soft drink", "cold drink", "soda", "aerated drink",

// 🧃 Juices
            "juice", "fruit juice", "real juice", "tropicana", "minute maid",
            "mixed juice", "fresh juice", "packed juice",

// ⚡ Energy / sports drinks
            "red bull", "monster", "sting", "energy drink", "gatorade", "sports drink",

// 🧋 Milk-based drinks
            "cold coffee", "iced coffee", "milkshake", "shake",
            "lassi", "sweet lassi", "salted lassi",
            "buttermilk", "chaas",
            "flavoured milk", "flavored milk", "chocolate milk",

// 🇮🇳 Indian drinks
            "nimbu pani", "lemon water", "lemon soda",
            "jaljeera", "aam panna", "sugarcane juice", "ganne ka juice",

// 🍹 Generic drink terms
            "drink", "beverage", "cool drink", "refreshment"
        )
    ),


    GROCERY(
        Icons.Default.ShoppingCart,
        Color(0xFF2E7D32),
        listOf(
            "grocery", "groceries",

            "atta chakki", "besan", "poha",
            "suji", "rava", "idli rice",
            "basmati rice", "dal chawal",
            // 🥦 vegetables
            "vegetable", "vegetables", "veggies", "sabzi", "sabji",
            "onion", "potato", "tomato", "carrot", "cabbage",
            "cauliflower", "beans", "peas", "brinjal", "eggplant",
            "lady finger", "okra", "spinach", "palak",
            "cucumber", "beetroot", "radish", "pumpkin",
            "ginger", "garlic", "chilli", "green chilli",

            "detergent", "washing powder", "washing liquid", "toilet cleaner",
            "phenyl", "floor cleaner", "dish wash",
            "dish soap", "scrubber", "tissue paper", "kitchen towel",
            // 🍎 fruits
            "fruit", "fruits", "apple", "banana", "orange", "mango",
            "grapes", "papaya", "pineapple", "watermelon",
            "pomegranate", "guava", "strawberry",
            // 🥜 nuts
            "nut", "nuts",
            "peanut", "peanuts", "groundnut", "moongphali",
            "almond", "almonds", "badam",
            "cashew", "cashews", "kaju",
            "pistachio", "pistachios", "pista",
            "walnut", "walnuts", "akhrot",
            "hazelnut", "hazelnuts",
            "macadamia",
            "pecan", "pecans",
            "brazil nut", "brazil nuts",
            "pine nut", "pine nuts", "chilgoza",
            "chestnut", "chestnuts",

            // 🌰 mixed & common
            "dry fruit", "dry fruits",
            "mixed nuts",
            "roasted nuts",
            "salted nuts", "raisin", "kishmis", "Dates",

            // 🥛 dairy
            "milk", "curd", "yogurt", "buttermilk", "paneer",
            "cheese", "butter", "ghee", "chesse",

            // 🍚 ration / staples
            "ration", "rice", "wheat", "atta", "flour",
            "dal", "lentil", "pulse", "chana", "rajma",
            "oil", "cooking oil", "sunflower oil", "gram",
            "salt", "sugar", "jaggery", "tea", "coffee" , "cold coffee", "namkeen", "sweet",

            // 🧺 daily items
            "bread", "egg", "eggs", "biscuit", "biscuits",
            "maggi", "noodles", "pasta", "sauce",
            "spices", "masala", "jeera", "haldi",
            "dhaniya", "mirchi",

            // 🛒 stores / apps
            "kirana", "ration shop", "supermarket",
            "dmart", "reliance smart", "big basket",
            "instamart", "blinkit", "zepto" , "walmart"
            ,
            "mosquito racket", "mosquito bat",
            "mosquito killer", "mosquito zapper",
            "mosquito repellent", "good night", "all out", "mortein" ,
            "gift", "present",
            "birthday gift",
            "anniversary gift",
            "wedding gift",

            "bouquet", "flower bouquet",
            "flower bunch", "flowers",
            "rose bouquet", "rose bunch",
            "flower gift", "bouquet gift",
            "boquet", "boquest", "broquet",

            "gift purchase",
            "gift item",
            "return gift", "bread", "egg", "eggs", "biscuit", "biscuits",
            "maggi", "noodles", "pasta", "sauce",


            "spices", "masala", "jeera", "haldi",
            "tasbeeh", "tasbih",
            "prayer cap", "topi",
            "namaz cap",
            "janamaz", "prayer mat",
            "miswak", "siwak",
            "zamzam", "zamzam water",
            "surma", "kohl",
            "dates", "khajoor",
            "ramadan items", "iftar items" ,
            "agarbatti", "incense stick", "dhoop",
            "dhoopbatti", "incense cone", "pooja samagri",
            "kapoor", "camphor", "diya",
            "cotton wicks", "batti", "pooja oil",
            "kumkum", "roli", "tilak",

            "ittar", "attar", "itr",
            "tasbeeh", "tasbih", "prayer cap",
            "topi", "namaz cap", "janamaz",
            "prayer mat", "miswak", "siwak",
            "zamzam", "zamzam water", "khajoor",

            "candle", "church candle", "prayer candle",
            "rosary", "cross pendant", "bible",
            "holy water", "church offering", "church donation",
            "christmas candle", "nativity set", "church gift",

            "pan", "tawa", "kadhai",
            "pressure cooker", "cooker gasket", "cooker whistle",
            "gas lighter", "matchbox", "kitchen knife",
            "knife", "peeler", "grater",
            "chopping board", "spoon", "steel spoon",
            "plate", "steel plate", "bowl",
            "steel bowl", "glass", "water bottle",
            "tiffin box", "lunch box", "storage box",
            "plastic container", "water jug", "bucket",
            "mug", "dustbin", "garbage bag",
            "cloth clip", "hanger", "rope",
            "bed sheet", "bedsheet", "pillow cover",
            "blanket", "towel", "bath towel",
            "hand towel", "doormat", "floor mat",
            "mosquito coil", "mosquito liquid", "mosquito spray",
            "phenyl bottle", "bleaching powder", "room freshener",
            "freshner",

            "rasan" , "rashan",
            "surf excel", "ariel", "tide",
            "rin", "wheel", "ghadi",
            "vim", "vim bar", "vim liquid",
            "lizol", "harpic", "domex",
            "odonil", "air wick", "room freshener",
            "good knight", "all out", "mortein",
            "hit spray", "baygon", "mosquito coil",
            "tata salt", "aashirvaad atta", "fortune oil",
            "mdh masala", "everest masala", "catch masala",
            "amul milk", "mother dairy", "nandini milk"
        )
    ),

    INVESTMENT(
        Icons.Default.TrendingUp,
        Color(0xFF0D47A1),
        listOf(
            "investment", "invest", "sip",
            "mutual fund", "mutualfund",
            "stock", "stocks", "share", "shares",
            "equity", "nse", "bse",
            "trading", "brokerage",
            "dividend", "portfolio",
            "crypto", "bitcoin", "ethereum",
            "gold etf", "etf", "grow",
            "zerodha", "groww", "upstox",
            "angel one", "angelone"
        )
    ),


    HOUSING(
        Icons.Default.HomeWork,
        Color(0xFF6D4C41), // neutral blue-grey
        listOf(
            "rent", "house rent", "room rent",
            "monthly rent", "flat rent",
            "pg rent", "pg", "Housing",
            "hostel rent", "hostel",
            "lease", "tenancy",
            "advance rent", "security deposit",
            "owner rent", "flat charge", "house charge",
            "maintenance", "society maintenance",
            "maintenance", "society maintenance" , "shoe rack", "shoe stand",
            "slipper stand", "chappal stand",
            "footwear stand", "shoe cabinet", "maintenance", "society maintenance",
            "slipper stand", "chappal stand",
            "footwear stand", "shoe cabinet" , "chair", "table", "bed",
            "sofa", "cupboard", "wardrobe",
            "mattress", "pillow", "bed sheet",
            "curtain", "curtains", "door mat",
            "floor mat", "carpet", "rug",
            "shoe rack", "shoe stand", "slipper stand",
            "mirror", "wall mirror", "wall clock",
            "electrician", "plumber", "carpenter",
            "home repair", "house repair", "maintenance",
            "paint", "wall paint", "paint service" , "ac repair", "fan repair", "geyser repair",
            "water motor", "water pump", "pipe repair"
        )
    ),

    SUBSCRIPTIONS(
        Icons.Default.Subscriptions,
        Color(0xFF00ACC1),
        listOf(
            // 🎬 Streaming platforms
            "netflix", "prime", "prime video", "amazon prime",
            "hotstar", "disney hotstar", "sonyliv", "sony liv",
            "zee5", "voot", "jiocinema", "mx player",

            // 🎵 Music
            "spotify", "apple music", "youtube music", "gaana", "wynk",

            // 📺 Video / Premium
            "youtube premium",

            // ☁️ Cloud / storage
            "google one", "icloud", "dropbox", "onedrive",

            // 💻 Software / tools
            "windows", "microsoft 365", "office 365",
            "adobe", "adobe cc", "photoshop subscription",
            "canva pro", "notion", "chatgpt", "openai",

            // 🎮 Gaming
            "xbox game pass", "game pass", "playstation plus", "ps plus",


            // 📰 News / others
            "subscription", "monthly subscription", "annual subscription",
            "auto debit", "autopay", "recurring payment"
        )
    ),

    MEDICINE(
        Icons.Default.MedicalServices,
        Color(0xFFE53935),
        listOf(
            "medicine","medicines", "tablet", "doctor", "hospital",
            "clinic", "medical", "pharmacy", "chemist", "medical store",
            "medicine shop", "tablet strip", "syrup", "medicine bill", "medical bill", "doctor fee",
            "clinic fee", "hospital fee", "medical test", "ct scan",
            "blood test", "health checkup", "diagnostic test", "mri" ,
            "medicine purchase", "pharmacy bill",
            "doctor consultation" , "injection",
            "capsule", "appointment", "bandage", "pain killer",
            "antibiotic", "medical prescription" , "apollo pharmacy", "medplus", "netmeds",
            "pharmeasy", "1mg", "tata 1mg",
            "dolo", "crocin", "paracetamol",
            "vicks", "strepsils", "digene"
        )
    ),

    TRANSPORT(
        Icons.Default.DirectionsTransit,
        Color(0xFF00C853),
        listOf(
    // general travel
            "transport",
    "travel", "trip", "road trip", "journey", "ride", "ride booking",

    // car / vehicle
    "car", "vehicle", "four wheeler", "auto", "auto rickshaw",
    "taxi", "cab",

    // ride apps
    "uber", "ola", "rapido",

    // bike / scooter
    "bike", "motorbike", "motor cycle",
    "two wheeler", "two-wheeler",
    "scooter", "moped",
    "bullet", "royal enfield",
    "pulsar", "apache",
    "ktm", "duke",
    "yamaha", "honda", "hero", "bajaj",

    // bike service / repair
    "bike service", "bike repair",
    "engine oil", "chain oil",
    "bike wash", "puncture",
    "tyre", "tire", "bike tyre",
    "bike mechanic", "bike accessories",

    // cab / taxi
    "cab ride", "taxi ride",
    "taxi booking", "cab booking",
    "taxi fare", "ride fare",
    "auto fare", "ride charge",

    // bus
    "bus", "bus ticket", "rtc", "ksrtc", "tsrtc",
    "private bus", "city bus",
    "bus fare", "bus journey",
    "bus travel", "bus depot",
    "bus booking", "bus transport",
    "redbus", "abhibus",
    "bus stand", "bus stop",
    "volvo bus", "sleeper bus", "ac bus",

    // train
    "train", "rail", "railway",
    "irctc", "rail ticket", "train ticket",
    "metro", "subway", "bullet train",
    "local train", "platform ticket",
    "tatkal", "railway station",
    "platform", "coach",
    "sleeper", "ac coach",

    // flight
    "flight", "air", "airline",
    "airplane", "aeroplane", "plane",
    "air ticket", "flight ticket",
    "boarding pass", "boarding",
    "checkin", "check-in",
    "departure", "arrival",
    "terminal fee", "airfare",
    "airport", "terminal",
    "baggage", "luggage",
    "indigo", "air india", "vistara",
    "akasa", "spacejet",

    // road expenses
    "parking", "toll", "fastag",
    "highway", "expressway",
    "parking fee", "toll tax",

            "parking", "toll", "fastag",
            "petrol", "fuel", "diesel",
            "petrol pump", "fuel pump",
            "petrol refill", "fuel refill",
            "petrol station", "fuel station",
            "cng", "cng refill",

          // EV charging
            "charging", "ev charging", "charging station" ,"hotel", "room",
            "stay", "lodge", "resort", "guest house", "oyo", "hostel", "Dhaba",
            "stay booking", "room booking", "hotel booking", "checkin hotel", "check-out",
            "car purchase", "buy car", "new car",
            "bike purchase", "buy bike", "new bike",
            "scooter purchase", "buy scooter", "vehicle purchase",
            "second hand car", "used car", "used bike",
            "car down payment", "bike down payment", "vehicle down payment" ,
            "swift", "baleno", "alto",
            "wagonr", "dzire", "i20",
            "creta", "venue", "seltos",
            "thar", "scorpio", "bolero",
            "fortuner", "innova", "verna",
            "city", "amaze", "magnite",

            "splendor", "pulsar", "apache",
            "duke", "ktm", "bullet",
            "royal enfield", "classic 350", "meteor",
            "r15", "fz", "mt15",
            "activa", "jupiter", "ntorq",
            "access", "dio", "burgman" ,

            "bmw", "audi", "mercedes",
            "range rover", "defender", "land rover",
            "jaguar", "volvo", "lexus",
            "porsche", "lamborghini", "ferrari", "Pagani" , "bugatti", "Toyota" , "Mahindra",
            "bentley", "rolls royce", "tesla", "tata" , "Ashok leyland" ,
            "mini cooper", "maserati", "aston martin",

            "merc", "g wagon", "g class"
      )
    ),

    OTHER(
        Icons.Default.Category,
        Color(0xFF9E9E9E),
        listOf()
    );

    // ✅ FIXED: Companion object is now correctly placed inside the class.
    companion object {
        fun detect(title: String): ExpenseCategory {

            val words = title
                .lowercase()
                .split(Regex("\\W+"))

            return ExpenseCategory.entries.firstOrNull { category ->
                category.keywords.any { keyword ->

                    val keyWords = keyword.lowercase().split(" ")

                    words.windowed(keyWords.size).any { window ->
                        window == keyWords
                    }
                }
            } ?: ExpenseCategory.OTHER
        }
    }
}