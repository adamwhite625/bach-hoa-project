const mongoose = require("mongoose");
const dotenv = require("dotenv");
const Category = require("../models/categoryModel");
const Product = require("../models/productModel");

dotenv.config();

const sampleData = [
  {
    category: {
      name: "Sữa, sản phẩm từ sữa",
      description: "Các sản phẩm sữa tươi, sữa hộp và các sản phẩm từ sữa",
    },
    products: [
      {
        name: "Sữa óc cho nguyên chất 137 độ C lốc 3x180ml",
        sku: "SUA001",
        price: 104000,
        quantity: 100,
        description: "Sữa óc cho nguyên chất 137 độ C, lốc 3x180ml",
        brand: "137 Degrees",
        image:
          "https://lh3.googleusercontent.com/bkuMdFtyOSXqCr5eKq5CNBCSdY6vkzyYljoycSi7dt2IySsqzbqXiPomPjeSKy1dX0nkGCbk9R1SOo1-Mqd616rbNm8Pm7QB=w230-rw",
      },
      {
        name: "Sữa óc cho 137 độ C nguyên chất hộp 1L",
        sku: "SUA002",
        price: 108000,
        quantity: 100,
        description: "Sữa óc cho 137 độ C nguyên chất, hộp 1L",
        brand: "137 Degrees",
        image:
          "https://lh3.googleusercontent.com/6jJi7Skof8FliTBt_fHtVU30WXVrJjthfmtnuEyg-7ZTplqmdrgLe84w1lIoP1XI2u5P8mY_sCKg54VTbPKuOJeI8JkYOacK=w230-rw",
      },
      {
        name: "Sản phẩm dinh dưỡng Ensure Original vani chai 237ml",
        sku: "ENS001",
        price: 42500,
        quantity: 80,
        description: "Sản phẩm dinh dưỡng Ensure Original vani, chai 237ml",
        brand: "Ensure",
        image:
          "https://lh3.googleusercontent.com/HVZ-kluEpyyp04SDtD4yjd9ukpN2sQ0Dd0vjD2sBuNpT7A--6pHkMLXjrZcVWItIIMnmGeD8s5-YlEuGLYAouLLIiw_FT8hh=w230-rw",
      },
      {
        name: "Nước uống sữa trái cây Nutri Boost cam mật ong lốc 6 chai x 297ml",
        sku: "NUT001",
        price: 49500,
        quantity: 120,
        description:
          "Nước uống sữa trái cây Nutri Boost cam mật ong, lốc 6 chai x 297ml",
        brand: "Nutriboost",
        image:
          "https://lh3.googleusercontent.com/s-gFYjX9-aHe1zlfbhVhvLyT69TKOJOiPR4j4iGWg9NKeL_b581S_wN1l78M61ZqsSF6YSUwZ-aDr5WLzjT_lysAQh0bQGIs=w230-rw",
      },
      {
        name: "Nước uống sữa trái cây Nutri Boost dâu yến mạch lốc 6 chai x 297ml",
        sku: "NUT002",
        price: 49500,
        quantity: 120,
        description:
          "Nước uống sữa trái cây Nutri Boost dâu yến mạch, lốc 6 chai x 297ml",
        brand: "Nutriboost",
        image:
          "https://lh3.googleusercontent.com/YQQWZSoDApcKu6ZKrRvS81hiuQaUT3JFqrY8uzG6lFTHgM2vOC9ZUEwShRDQkUUgVpmhdu4BoTsBoz8kNDBjmXiJNMdR_ttD=w230-rw",
      },
      {
        name: "Sữa lên men Betagen hương cam chai 700ml",
        sku: "BET001",
        price: 42000,
        quantity: 90,
        description: "Sữa lên men Betagen hương cam, chai 700ml",
        brand: "Betagen",
        image:
          "https://lh3.googleusercontent.com/390dzxMbAXeLJu54cdo6Y_HuMTW7v--Wr3yuDW5UXnBaQtFYWm4q5TNCX5clXFsQQrQvUC3hFvJHR5roEhD17X7iH6WTfkde=w230-rw",
      },
      {
        name: "Phô mai La Vache Quirit truyền thống hộp 224g",
        sku: "PHO001",
        price: 64000,
        quantity: 70,
        description: "Phô mai La Vache Quirit truyền thống, hộp 224g",
        brand: "Con Bò Cười",
        image:
          "https://lh3.googleusercontent.com/zWQS4Sm4YfbcTfPu-t7O5Taplji0A4kd9c8xutUcDQ2LWnbzl39psKYbeQCPqcO7u_PneJwlFa7nFw5PwZjnMXEhjx3_eOIw=w230-rw",
      },
    ],
  },
  {
    category: {
      name: "Rau củ, trái cây",
      description: "Rau củ tươi sạch, trái cây tự nhiên",
    },
    products: [
      {
        name: "Bí đỏ tròn Co.op Select kg",
        sku: "VEG001",
        price: 25900,
        quantity: 150,
        description: "Bí đỏ tròn Co.op Select, tính theo kg",
        brand: "Co.op Select",
        image:
          "https://lh3.googleusercontent.com/x4Is7-UXHRrqoFI6l0oqN2ggxQkhsKzyD_ROPG_X37dUkNW8q1sMZKuqieqoQp5XOGcmmg7cZFH9RIhZgVZrRJUpTE6GYXTD=w230-rw",
      },
      {
        name: "Mướp hương Co.op Select kg",
        sku: "VEG002",
        price: 26500,
        quantity: 140,
        description: "Mướp hương Co.op Select, tính theo kg",
        brand: "Co.op Select",
        image:
          "https://lh3.googleusercontent.com/p2DQlYByzrw4DFqP1w4D59be9gdDhVdjg5IYe56LyIp9hceLhKEU3sKKONagRyqjbWxbc0m6HhUYYKznDe0b2nbjg6UctVQf=w230-rw",
      },
      {
        name: "Khổ qua Co.op Select kg",
        sku: "VEG003",
        price: 33500,
        quantity: 120,
        description: "Khổ qua Co.op Select, tính theo kg",
        brand: "Co.op Select",
        image:
          "https://lh3.googleusercontent.com/FJ3O9mfFuKo4UoTeKvzKGFxUXz27mGRIu5gtumDB5AE5WnUe3MKuRTuPiVgV5B8JZcZ1q_6PX-LlrKCtxd890XPkWlpwWOfhmg=w230-rw",
      },
    ],
  },
  {
    category: {
      name: "Gia vị, thực phẩm khô",
      description: "Gia vị nấu ăn và các sản phẩm thực phẩm khô",
    },
    products: [
      {
        name: "Mì Reeva 3 Miền tôm chua cay thùng 30 gói x 65g",
        sku: "GIA001",
        price: 94000,
        quantity: 80,
        description: "Mì Reeva 3 Miền tôm chua cay, thùng 30 gói x 65g",
        brand: "3 Miền",
        image:
          "https://lh3.googleusercontent.com/bIhVx5u6dz6G48RzHIP3bQrQVTSiRBoTupC19BlagJoQf1MgLTeIdtOBOqvYN45CEwojgzBn-a_pC9elenOb1n_MpLHU9KHNaQ=w230-rw",
      },
      {
        name: "Mì Hảo Hảo vị tôm chua cay gói 75g",
        sku: "GIA002",
        price: 4300,
        quantity: 200,
        description: "Mì Hảo Hảo vị tôm chua cay, gói 75g",
        brand: "Hảo Hảo",
        image:
          "https://lh3.googleusercontent.com/d2-sZeltwojyYhLEQ0luhuWAIlZrm5zAXc2FoJNEzf0vTTKII56c_YOniCLLBO9S-W1VZ4UwG-zCI_33CMvJMfwh7G6LJfo=w230-rw",
      },
      {
        name: "Mì Đệ Nhất vị thịt bằm gói 83g",
        sku: "GIA003",
        price: 7800,
        quantity: 180,
        description: "Mì Đệ Nhất vị thịt bằm, gói 83g",
        brand: "Đệ Nhất",
        image:
          "https://lh3.googleusercontent.com/D9hMXaNygECqsenRWNjhvLhHSEFUBe5PXS1EwL5VCPstHPS6MQxZtOGo3AjOV-LKpPu0KY5W8Va0Gz0216DZk0udo5GeZGe-=w230-rw",
      },
      {
        name: "Hủ tiếu Nhịp Sống vị nam vang 69g",
        sku: "GIA004",
        price: 9700,
        quantity: 160,
        description: "Hủ tiếu Nhịp Sống vị nam vang, gói 69g",
        brand: "Nhịp Sống",
        image:
          "https://lh3.googleusercontent.com/wF0uew5aFdxCRaq-4szyLGhyml4BWKl-LwLrcpGo9_XEDeW7VifHYT03HjhphFXvp5ohc6kWIccZPRj-c6D7U-s820bH0bw=w230-rw",
      },
    ],
  },
  {
    category: {
      name: "Thịt, trứng, hải sản",
      description: "Thịt tươi, trứng gà, hải sản tươi sống",
    },
    products: [
      {
        name: "Thịt heo xay 400g - CJ",
        sku: "MEAT001",
        price: 49000,
        quantity: 100,
        description: "Thịt heo xay 400g, hãng CJ",
        brand: "CJ Food",
        image:
          "https://lh3.googleusercontent.com/yHPvzzKrkwpfKKdtI4Rb81jjWs2Ucsi0QTI0Q6jHX1fs9QxowrOBaMHeasAKc15_g5kvIHXSShY9LwhUVgI6K9P1LF0OTensSQ=w230-rw",
      },
      {
        name: "File gà công nghiệp 500g - Leboucher",
        sku: "MEAT002",
        price: 41000,
        quantity: 90,
        description: "File gà công nghiệp 500g, hãng Leboucher",
        brand: "Leboucher",
        image:
          "https://lh3.googleusercontent.com/-tZkO5LvOqIZZ01Jo9n-y1fuzLpPbVens5cHboyGCDKUfA3hOmQZCMYMxQESTNdFW66EWQAebjq1Hf2rFe_KQtLv6ZFtPQcI=w230-rw",
      },
      {
        name: "Nghêu sạch Lenger 600g/túi",
        sku: "MEAT003",
        price: 32900,
        quantity: 110,
        description: "Nghêu sạch Lenger, 600g/túi",
        brand: "Lenger",
        image:
          "https://lh3.googleusercontent.com/nLQWMUE2ghfMv3uH3L5SkrA-ZVsiBJaztGMQglMpllKsdmEun25Zedxq3e70ql4CMspGsvhGq87mnmg6LWUH7GXr-jqBXZpl=w230-rw",
      },
      {
        name: "Trứng gà tươi TAFA 600g - Hộp nhựa",
        sku: "MEAT004",
        price: 28000,
        quantity: 130,
        description: "Trứng gà tươi TAFA 600g, hộp nhựa",
        brand: "Tafa",
        image:
          "https://lh3.googleusercontent.com/p5AagndSCHDhqfCkIlbS6KuNPVHR3EBjJFRlrcHJKpl4r9xCNgF6Y6WXrrxgM4R77nG5bjF2kdYu48TDh1gffJog5PD1wUGE=w230-rw",
      },
    ],
  },
];

const seedProducts = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB...");

    // Thêm categories và products
    for (const item of sampleData) {
      // Kiểm tra category đã tồn tại chưa
      let category = await Category.findOne({ name: item.category.name });

      if (!category) {
        // Tạo category mới nếu chưa tồn tại
        category = await Category.create({
          name: item.category.name,
          description: item.category.description,
          image:
            "https://res.cloudinary.com/daeeumk1i/image/upload/v1/categories/placeholder.jpg",
        });
        console.log(`✓ Created category: ${item.category.name}`);
      } else {
        console.log(`→ Category already exists: ${item.category.name}`);
      }

      // Thêm products cho category
      for (const productData of item.products) {
        // Kiểm tra sản phẩm đã tồn tại chưa
        const existingProduct = await Product.findOne({ sku: productData.sku });

        if (!existingProduct) {
          await Product.create({
            ...productData,
            category: category._id,
          });
          console.log(`  ✓ Created product: ${productData.name}`);
        } else {
          console.log(`  → Product already exists: ${productData.name}`);
        }
      }
    }

    console.log("\n✓ Seeding completed successfully!");
    process.exit();
  } catch (error) {
    console.error("Error seeding products:", error);
    process.exit(1);
  }
};

seedProducts();
