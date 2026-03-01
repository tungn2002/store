import React, { useState, useEffect } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Link, NavLink, useHistory } from "react-router-dom";
import {
  EditIcon,
  EyeIcon,
  GridViewIcon,
  HomeIcon,
  ListViewIcon,
  TrashIcon,
  AddIcon,
} from "../icons";
import {
  Card,
  CardBody,
  Label,
  Select,
  Button,
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableFooter,
  Avatar,
  Badge,
  Pagination,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Input,
} from "@windmill/react-ui";
import Icon from "../components/Icon";
import { useToast } from "../utils/toast";
import {
  getProducts,
  deleteProduct,
  getCategories,
  getBrands,
} from "../utils/productsApi";

const ProductsAll = () => {
  const history = useHistory();
  const toast = useToast();
  const [view, setView] = useState("grid");
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [resultsPerPage, setResultsPerPage] = useState(10);
  const [pagination, setPagination] = useState({
    totalItems: 0,
    totalPages: 0,
    hasNext: false,
  });

  // Filters
  const [searchName, setSearchName] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("");
  const [selectedBrand, setSelectedBrand] = useState("");
  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDeleteProduct, setSelectedDeleteProduct] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadCategoriesAndBrands();
  }, []);

  useEffect(() => {
    loadProducts();
  }, [page, resultsPerPage, searchName, selectedCategory, selectedBrand]);

  const loadCategoriesAndBrands = async () => {
    try {
      const [catRes, brandRes] = await Promise.all([
        getCategories(0, 100),
        getBrands(0, 100),
      ]);
      setCategories(catRes.items || []);
      setBrands(brandRes.items || []);
    } catch (error) {
      console.error("Error loading categories/brands:", error);
    }
  };

  const loadProducts = async () => {
    setLoading(true);
    try {
      const result = await getProducts(
        page - 1,
        resultsPerPage,
        "id",
        searchName,
        selectedCategory,
        selectedBrand
      );
      setData(result.items);
      setPagination({
        totalItems: result.totalItems,
        totalPages: result.totalPages,
        hasNext: result.hasNext,
      });
    } catch (error) {
      console.error("Error loading products:", error);
      toast.error("Failed to load products");
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    setPage(1);
    loadProducts();
  };

  const handleClearFilters = () => {
    setSearchName("");
    setSelectedCategory("");
    setSelectedBrand("");
    setPage(1);
  };

  const openDeleteModal = (product) => {
    setSelectedDeleteProduct(product);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedDeleteProduct(null);
  };

  const handleDelete = async () => {
    if (!selectedDeleteProduct) return;
    setSubmitting(true);
    try {
      await deleteProduct(selectedDeleteProduct.id);
      toast.success("Product deleted successfully");
      closeModal();
      loadProducts();
    } catch (error) {
      console.error("Error deleting product:", error);
      const message = error.response?.data?.message || "Failed to delete product";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleChangeView = () => {
    setView(view === "list" ? "grid" : "list");
  };

  return (
    <div>
      <PageTitle>All Products</PageTitle>

      {/* Breadcrumb */}
      <div className="flex text-gray-800 dark:text-gray-300 mb-4">
        <div className="flex items-center text-purple-600">
          <Icon className="w-5 h-5" aria-hidden="true" icon={HomeIcon} />
          <NavLink exact to="/app/dashboard" className="mx-2">
            Dashboard
          </NavLink>
        </div>
        <span className="mx-2">{" > "} All Products</span>
      </div>

      {/* Filters */}
      <Card className="mt-5 mb-5 shadow-md">
        <CardBody>
          <div className="flex items-center justify-between flex-wrap gap-4">
            <div className="flex items-center flex-wrap gap-3">
              <Input
                type="text"
                placeholder="Search by name..."
                value={searchName}
                onChange={(e) => setSearchName(e.target.value)}
                onKeyPress={(e) => e.key === "Enter" && handleSearch()}
                className="w-64"
              />
              <Label>
                <Select
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                >
                  <option value="">All Categories</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </Select>
              </Label>
              <Label>
                <Select
                  value={selectedBrand}
                  onChange={(e) => setSelectedBrand(e.target.value)}
                >
                  <option value="">All Brands</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>
                      {brand.name}
                    </option>
                  ))}
                </Select>
              </Label>
              <Button onClick={handleSearch} size="small">
                Search
              </Button>
              <Button
                onClick={handleClearFilters}
                size="small"
                layout="outline"
              >
                Clear
              </Button>
            </div>
            <div className="flex items-center gap-2">
              <Button
                icon={view === "list" ? ListViewIcon : GridViewIcon}
                className="p-2"
                aria-label="Toggle View"
                onClick={handleChangeView}
              />
              <Button
                icon={AddIcon}
                onClick={() => history.push("/app/add-product")}
                size="small"
              >
                Add Product
              </Button>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Delete Modal */}
      <Modal isOpen={isModalOpen} onClose={closeModal}>
        <ModalHeader className="flex items-center">
          <Icon icon={TrashIcon} className="w-6 h-6 mr-3" />
          Delete Product
        </ModalHeader>
        <ModalBody>
          Are you sure you want to delete product{" "}
          {selectedDeleteProduct && `"${selectedDeleteProduct.name}"`}
          ?
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleDelete} disabled={submitting}>
              {submitting ? "Deleting..." : "Delete"}
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button
              block
              size="large"
              layout="outline"
              onClick={closeModal}
              disabled={submitting}
            >
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button
              block
              size="large"
              onClick={handleDelete}
              disabled={submitting}
            >
              {submitting ? "Deleting..." : "Delete"}
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Product Views */}
      {loading ? (
        <div className="flex justify-center items-center py-12">
          <span>Loading...</span>
        </div>
      ) : view === "list" ? (
        <TableContainer className="mb-8">
          <Table>
            <TableHeader>
              <tr>
                <TableCell>ID</TableCell>
                <TableCell>Image</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Brand</TableCell>
                <TableCell>Rating</TableCell>
                <TableCell>Action</TableCell>
              </tr>
            </TableHeader>
            <TableBody>
              {data.length > 0 ? (
                data.map((product) => (
                  <TableRow key={product.id}>
                    <TableCell>{product.id}</TableCell>
                    <TableCell>
                      <Avatar
                        src={product.image || "https://via.placeholder.com/50"}
                        alt={product.name}
                      />
                    </TableCell>
                    <TableCell>
                      <p className="font-semibold">{product.name}</p>
                    </TableCell>
                    <TableCell>{product.category?.name || "N/A"}</TableCell>
                    <TableCell>{product.brand?.name || "N/A"}</TableCell>
                    <TableCell>
                      <div className="flex items-center">
                        <span className="text-yellow-500">★</span>
                        <span className="ml-1">
                          {product.variants?.[0]?.stockQuantity >= 0 ? "4.5" : "N/A"}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex">
                        <Link to={`/app/products/${product.id}`}>
                          <Button
                            icon={EyeIcon}
                            className="mr-3"
                            aria-label="View"
                            size="small"
                          />
                        </Link>
                        <Link to={`/app/products/${product.id}/edit`}>
                          <Button
                            icon={EditIcon}
                            className="mr-3"
                            layout="outline"
                            aria-label="Edit"
                            size="small"
                          />
                        </Link>
                        <Button
                          icon={TrashIcon}
                          layout="outline"
                          onClick={() => openDeleteModal(product)}
                          aria-label="Delete"
                          size="small"
                        />
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan="7" className="text-center">
                    No products found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
          <TableFooter>
            <Pagination
              totalResults={pagination.totalItems}
              resultsPerPage={resultsPerPage}
              label="Table navigation"
              onChange={(p) => setPage(p)}
            />
          </TableFooter>
        </TableContainer>
      ) : (
        <>
          <div className="grid gap-4 grid-cols-2 md:grid-cols-3 lg:grid-cols-4 mb-8">
            {data.length > 0 ? (
              data.map((product) => (
                <div key={product.id}>
                  <Card>
                    <img
                      className="object-cover w-full h-48"
                      src={product.image || "https://via.placeholder.com/300"}
                      alt={product.name}
                    />
                    <CardBody>
                      <div className="mb-3 flex items-center justify-between">
                        <p className="font-semibold truncate text-gray-600 dark:text-gray-300">
                          {product.name}
                        </p>
                      </div>
                      <p className="mb-2 text-purple-500 font-bold text-lg">
                        ${product.variants?.[0]?.price?.toFixed(2) || "0.00"}
                      </p>
                      <p className="mb-2 text-sm text-gray-500">
                        {product.category?.name} • {product.brand?.name}
                      </p>
                      <div className="flex items-center justify-between">
                        <div>
                          <Link to={`/app/products/${product.id}`}>
                            <Button
                              icon={EyeIcon}
                              className="mr-3"
                              aria-label="View"
                              size="small"
                            />
                          </Link>
                        </div>
                        <div>
                          <Link to={`/app/products/${product.id}/edit`}>
                            <Button
                              icon={EditIcon}
                              className="mr-3"
                              layout="outline"
                              aria-label="Edit"
                              size="small"
                            />
                          </Link>
                          <Button
                            icon={TrashIcon}
                            layout="outline"
                            onClick={() => openDeleteModal(product)}
                            aria-label="Delete"
                            size="small"
                          />
                        </div>
                      </div>
                    </CardBody>
                  </Card>
                </div>
              ))
            ) : (
              <div className="col-span-full text-center py-12 text-gray-500">
                No products found
              </div>
            )}
          </div>
          <Pagination
            totalResults={pagination.totalItems}
            resultsPerPage={resultsPerPage}
            label="Table navigation"
            onChange={(p) => setPage(p)}
          />
        </>
      )}
    </div>
  );
};

export default ProductsAll;
