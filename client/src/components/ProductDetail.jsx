import React, { useState } from 'react';

const ProductDetail = ({ product, toggleView }) => {
  const [selectedColor, setSelectedColor] = useState('Đỏ');
  const [selectedSize, setSelectedSize] = useState('40');
  const [mainImage, setMainImage] = useState(product.img);

  const changeMainImg = (src) => {
    setMainImage(src);
  };

  const selectSize = (size) => {
    setSelectedSize(size);
  };

  const selectColor = (color) => {
    setSelectedColor(color);
  };

  return (
    <div className="max-w-[1200px] mx-auto px-12 py-8 animate-fade-in">
      {/* Breadcrumb */}
      <nav className="text-xs text-gray-500 mb-8 uppercase tracking-wider">
        <a href="javascript:void(0)" onClick={() => toggleView('home')} className="hover:text-red-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <a href="javascript:void(0)" onClick={() => toggleView('category', 'Sản phẩm')} className="hover:text-red-600">Giày Nam</a>
        <span className="mx-2">/</span>
        <span className="text-gray-800 font-bold">{product.name}</span>
      </nav>

      <div className="flex flex-col lg:flex-row gap-12 bg-white p-6 rounded-lg shadow-sm mb-12">
        {/* Left: Gallery */}
        <div className="w-full lg:w-1/2 flex flex-col gap-4">
          <div className="border border-gray-100 rounded-lg overflow-hidden">
            <img id="main-product-img" src={mainImage} alt="Main Product" className="w-full h-auto object-cover aspect-square transition-opacity duration-300" />
          </div>
          <div className="flex gap-4">
            {product.gallery.map((img, index) => (
              <div
                key={index}
                className={`w-20 h-20 border-2 ${mainImage === img ? 'thumb-active border-red-600' : 'border-gray-100'} rounded overflow-hidden cursor-pointer hover:border-red-600 transition`}
                onClick={() => changeMainImg(img)}
              >
                <img src={img} className="w-full h-full object-cover" />
              </div>
            ))}
          </div>
        </div>

        {/* Right: Info & Selectors */}
        <div className="w-full lg:w-1/2 space-y-6">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mb-2 uppercase">{product.name}</h1>
            <div className="flex items-center gap-4">
              <span className="text-3xl font-bold text-red-600">{product.price.toLocaleString()}₫</span>
              {product.oldPrice && <span className="text-lg text-gray-400 line-through">{product.oldPrice.toLocaleString()}₫</span>}
            </div>
          </div>

          <div className="border-t border-b py-4 space-y-4">
            <div>
              <p className="text-sm font-bold uppercase mb-3">Màu sắc: <span className="text-red-600 ml-1">{selectedColor}</span></p>
              <div className="flex gap-3">
                <button onClick={() => selectColor('Đỏ')} className={`w-10 h-10 rounded-full bg-red-600 border-2 border-white ${selectedColor === 'Đỏ' ? 'ring-2 ring-red-600' : 'hover:ring-2 hover:ring-gray-300'}`}></button>
                <button onClick={() => selectColor('Đen')} className={`w-10 h-10 rounded-full bg-black border-2 border-white ${selectedColor === 'Đen' ? 'ring-2 ring-red-600' : 'hover:ring-2 hover:ring-gray-300'}`}></button>
                <button onClick={() => selectColor('Trắng')} className={`w-10 h-10 rounded-full bg-white border-2 border-gray-200 ${selectedColor === 'Trắng' ? 'ring-2 ring-red-600' : 'hover:ring-2 hover:ring-gray-300'}`}></button>
              </div>
            </div>

            <div>
              <p className="text-sm font-bold uppercase mb-3">Chọn Kích cỡ (Size):</p>
              <div className="grid grid-cols-4 md:grid-cols-6 gap-2">
                {['38', '39', '40', '41'].map(size => (
                  <button
                    key={size}
                    onClick={() => selectSize(size)}
                    className={`border py-2 text-sm hover:border-red-600 transition ${selectedSize === size ? 'border-red-600 bg-red-50 text-red-600 font-bold' : 'border-gray-200'}`}
                  >
                    {size}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4">
            <button className="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-md uppercase tracking-wider transition shadow-lg">
              MUA NGAY
            </button>
          </div>
        </div>
      </div>

      {/* Description Section */}
      <div className="bg-white p-6 md:p-10 rounded-lg shadow-sm mb-12">
        <div className="border-b border-gray-200 mb-8">
          <h2 className="text-xl font-bold uppercase border-b-2 border-black inline-block pb-4">Mô tả sản phẩm</h2>
        </div>
        <div className="prose max-w-none text-gray-600 leading-relaxed">
          <p className="mb-4">Nike Air Max 90 vẫn giữ nguyên những giá trị cốt lõi từ thiết kế ban đầu năm 1990, kết hợp với công nghệ hiện đại mang lại cảm giác thoải mái tối ưu. Đây là mẫu sneaker biểu tượng không thể thiếu trong tủ giày của mọi tín đồ thời trang.</p>
          <ul className="list-disc pl-5 space-y-2 mb-6">
            <li>Chất liệu: Da cao cấp phối lưới thoáng khí.</li>
            <li>Đế Air Max: Giảm chấn cực tốt, mang lại độ đàn hồi cao.</li>
            <li>Đế ngoài: Cao su với vân Waffle bám đường vượt trội.</li>
            <li>Kiểu dáng: Thể thao, năng động, dễ dàng phối đồ.</li>
          </ul>
          <p>Mẫu giày này phù hợp cho cả đi làm, đi chơi và tập luyện nhẹ nhàng. Hệ thống Giày Xshop cam kết hàng chính hãng 100%, bảo hành keo chỉ trọn đời.</p>
        </div>
      </div>

      {/* Review Section */}
      <div className="bg-white p-6 md:p-10 rounded-lg shadow-sm">
        <div className="border-b border-gray-200 mb-8">
          <h2 className="text-xl font-bold uppercase border-b-2 border-black inline-block pb-4">Đánh giá khách hàng</h2>
        </div>

        <div className="flex flex-col md:flex-row gap-10 mb-10 items-center">
          <div className="text-center md:border-r md:pr-10">
            <div className="text-5xl font-bold text-red-600 mb-2">4.8/5</div>
            <div className="flex text-yellow-400 mb-1">
              <i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star-half-alt"></i>
            </div>
            <p className="text-gray-500 text-sm">(124 đánh giá)</p>
          </div>

          <div className="flex-1 space-y-2 w-full max-w-xs">
            <div className="flex items-center gap-4">
              <span className="text-xs font-bold w-12">5 Sao</span>
              <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden"><div className="h-full bg-red-600" style={{ width: '85%' }}></div></div>
              <span className="text-xs text-gray-500 w-8 text-right">85%</span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-xs font-bold w-12">4 Sao</span>
              <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden"><div className="h-full bg-red-600" style={{ width: '10%' }}></div></div>
              <span className="text-xs text-gray-500 w-8 text-right">10%</span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-xs font-bold w-12">3 Sao</span>
              <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden"><div className="h-full bg-red-600" style={{ width: '3%' }}></div></div>
              <span className="text-xs text-gray-500 w-8 text-right">3%</span>
            </div>
            <div className="flex items-center gap-4">
              <span className="text-xs font-bold w-12">2 Sao</span>
              <div className="flex-1 h-2 bg-gray-100 rounded-full overflow-hidden"><div className="h-full bg-red-600" style={{ width: '2%' }}></div></div>
              <span className="text-xs text-gray-500 w-8 text-right">2%</span>
            </div>
          </div>
        </div>

        {/* Review Filters */}
        <div className="flex flex-wrap gap-2 mb-8 border-t border-b py-4">
          <span className="text-sm font-bold w-full mb-2 uppercase">Lọc xem đánh giá:</span>
          <button className="px-4 py-2 border border-red-600 bg-red-600 text-white rounded-full text-xs font-bold transition">Tất cả (124)</button>
          <button className="px-4 py-2 border border-gray-200 hover:border-red-600 rounded-full text-xs transition">5 Sao (105)</button>
          <button className="px-4 py-2 border border-gray-200 hover:border-red-600 rounded-full text-xs transition">4 Sao (12)</button>
          <button className="px-4 py-2 border border-gray-200 hover:border-red-600 rounded-full text-xs transition">3 Sao (4)</button>
          <button className="px-4 py-2 border border-gray-200 hover:border-red-600 rounded-full text-xs transition">Có hình ảnh (42)</button>
        </div>

        {/* Review List */}
        <div className="space-y-8">
          <div className="border-b border-gray-100 pb-8">
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center font-bold text-gray-600">TH</div>
                <div>
                  <h4 className="font-bold text-sm">Trần Hoàng</h4>
                  <div className="flex text-yellow-400 text-[10px]">
                    <i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i>
                  </div>
                </div>
              </div>
              <span className="text-xs text-gray-400">15/05/2024</span>
            </div>
            <p className="text-sm text-gray-600 mb-3">Giày rất đẹp, đúng như mô tả. Shop tư vấn size rất nhiệt tình, mang vừa in không bị chật hay rộng quá. Sẽ tiếp tục ủng hộ!</p>
            <div className="flex gap-2">
              <img src="https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=100" className="w-20 h-20 object-cover rounded border" />
            </div>
            <div className="mt-4 bg-gray-50 p-4 rounded text-xs border-l-4 border-red-600">
              <p className="font-bold mb-1">Xshop Phản hồi:</p>
              <p>Cảm ơn bạn Hoàng đã tin tưởng lựa chọn Giày Xshop. Hy vọng được phục vụ bạn trong những đơn hàng tiếp theo!</p>
            </div>
          </div>

          <div className="border-b border-gray-100 pb-8">
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-gray-200 rounded-full flex items-center justify-center font-bold text-gray-600">MN</div>
                <div>
                  <h4 className="font-bold text-sm">Minh Ngọc</h4>
                  <div className="flex text-yellow-400 text-[10px]">
                    <i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="fas fa-star"></i><i className="far fa-star"></i>
                  </div>
                </div>
              </div>
              <span className="text-xs text-gray-400">10/05/2024</span>
            </div>
            <p className="text-sm text-gray-600">Giày đi êm chân, màu đỏ rất nổi bật. Tuy nhiên hộp hơi bị móp một chút do vận chuyển xa, nhưng giày bên trong vẫn ok.</p>
          </div>
        </div>

        <div className="text-center mt-8">
          <button className="text-sm font-bold text-red-600 hover:underline">Xem thêm tất cả đánh giá <i className="fas fa-chevron-down ml-1"></i></button>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;