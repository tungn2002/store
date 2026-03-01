import React, { useState, useEffect } from "react";
import { NavLink, useParams } from "react-router-dom";
import Icon from "../components/Icon";
import PageTitle from "../components/Typography/PageTitle";
import { HomeIcon, AddIcon, EditIcon, TrashIcon } from "../icons";
import {
  Card,
  CardBody,
  Badge,
  Button,
  Avatar,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableBody,
  TableContainer,
  TableFooter,
  Pagination,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Label,
  Input,
  Select,
} from "@windmill/react-ui";
import { useToast } from "../utils/toast";
import {
  getProduct,
  getProductVariants,
  deleteProductVariant,
  createProductVariant,
  updateProductVariant,
} from "../utils/productsApi";

const SingleProduct = () => {
  const { id } = useParams();
  const toast = useToast();

  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);

  // Variants state
  const [variants, setVariants] = useState([]);
  const [variantsLoading, setVariantsLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [resultsPerPage] = useState(10);
  const [pagination, setPagination] = useState({
    totalItems: 0,
    totalPages: 0,
  });

  // Modal states
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState("add"); // 'add' or 'edit'
  const [selectedVariant, setSelectedVariant] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // Form state
  const [size, setSize] = useState("");
  const [color, setColor] = useState("");
  const [price, setPrice] = useState("");
  const [stockQuantity, setStockQuantity] = useState("");
  const [variantImage, setVariantImage] = useState(null);
  const [variantImagePreview, setVariantImagePreview] = useState(null);

  useEffect(() => {
    loadProduct();
  }, [id]);

  useEffect(() => {
    loadVariants();
  }, [page, id]);

  const loadProduct = async () => {
    try {
      const data = await getProduct(id);
      setProduct(data);
    } catch (error) {
      console.error("Error loading product:", error);
      toast.error("Failed to load product details");
    } finally {
      setLoading(false);
    }
  };

  const loadVariants = async () => {
    setVariantsLoading(true);
    try {
      const result = await getProductVariants(id, page - 1, resultsPerPage, "id");
      setVariants(result.items || []);
      setPagination({
        totalItems: result.totalItems,
        totalPages: result.totalPages,
      });
    } catch (error) {
      console.error("Error loading variants:", error);
      toast.error("Failed to load variants");
    } finally {
      setVariantsLoading(false);
    }
  };

  const openAddModal = () => {
    setModalMode("add");
    setSize("");
    setColor("");
    setPrice("");
    setStockQuantity("");
    setVariantImage(null);
    setVariantImagePreview(null);
    setIsModalOpen(true);
  };

  const openEditModal = (variant) => {
    setModalMode("edit");
    setSelectedVariant(variant);
    setSize(variant.size || "");
    setColor(variant.color || "");
    setPrice(variant.price?.toString() || "");
    setStockQuantity(variant.stockQuantity?.toString() || "");
    setVariantImage(null);
    setVariantImagePreview(variant.image || null);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedVariant(null);
  };

  const handleVariantImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setVariantImage(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setVariantImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!price || parseFloat(price) <= 0) {
      toast.error("Valid price is required");
      return;
    }

    setSubmitting(true);

    try {
      const formData = new FormData();
      formData.append("productId", id);
      formData.append("price", price);
      formData.append("stockQuantity", stockQuantity || 0);
      if (size) formData.append("size", size);
      if (color) formData.append("color", color);
      if (variantImage) formData.append("image", variantImage);

      if (modalMode === "add") {
        await createProductVariant(id, formData);
        toast.success("Variant added successfully");
      } else {
        await updateProductVariant(id, selectedVariant.id, formData);
        toast.success("Variant updated successfully");
      }

      closeModal();
      loadVariants();
    } catch (error) {
      console.error("Error saving variant:", error);
      const message = error.response?.data?.message || "Failed to save variant";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteVariant = async (variant) => {
    if (!window.confirm(`Delete variant "${variant.size || ""} ${variant.color || ""}"?`)) {
      return;
    }

    try {
      await deleteProductVariant(id, variant.id);
      toast.success("Variant deleted successfully");
      loadVariants();
    } catch (error) {
      console.error("Error deleting variant:", error);
      const message = error.response?.data?.message || "Failed to delete variant";
      toast.error(message);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <span>Loading...</span>
      </div>
    );
  }

  if (!product) {
    return (
      <div className="flex justify-center items-center py-12">
        <span>Product not found</span>
      </div>
    );
  }

  return (
    <div>
      <PageTitle>Product Details</PageTitle>

      {/* Breadcrumb */}
      <div className="flex text-gray-800 dark:text-gray-300">
        <div className="flex items-center text-purple-600">
          <Icon className="w-5 h-5" aria-hidden="true" icon={HomeIcon} />
          <NavLink exact to="/app/dashboard" className="mx-2">
            Dashboard
          </NavLink>
        </div>
        <span className="mx-2">{" > "}</span>
        <NavLink exact to="/app/all-products" className="mx-2 text-purple-600">
          All Products
        </NavLink>
        <span className="mx-2">{" > "}</span>
        <p className="text-gray-600 dark:text-gray-300">{product.name}</p>
      </div>

      {/* Product Info */}
      <Card className="my-8 shadow-md">
        <CardBody>
          <div className="grid grid-col items-center md:grid-cols-2 lg:grid-cols-2">
            <div>
              <img
                src={product.image || "https://via.placeholder.com/400"}
                alt={product.name}
                className="w-full rounded-lg"
              />
            </div>

            <div className="mx-8 pt-5 md:pt-0">
              <h1 className="text-3xl mb-4 font-semibold text-gray-700 dark:text-gray-200">
                {product.name}
              </h1>

              <Badge
                type={product.variants?.[0]?.stockQuantity > 0 ? "success" : "danger"}
                className="mb-2"
              >
                {product.variants?.[0]?.stockQuantity > 0 ? "In Stock" : "Out of Stock"}
              </Badge>

              <p className="mb-3 text-sm text-gray-800 dark:text-gray-300">
                {product.description || "No description available"}
              </p>

              <div className="mb-3">
                <p className="text-sm text-gray-500">Category</p>
                <p className="font-semibold text-gray-700 dark:text-gray-200">
                  {product.category?.name || "N/A"}
                </p>
              </div>

              <div className="mb-3">
                <p className="text-sm text-gray-500">Brand</p>
                <p className="font-semibold text-gray-700 dark:text-gray-200">
                  {product.brand?.name || "N/A"}
                </p>
              </div>

              <div className="mb-3">
                <p className="text-sm text-gray-500">Price</p>
                <p className="text-2xl font-bold text-purple-600">
                  ${product.variants?.[0]?.price?.toFixed(2) || "0.00"}
                </p>
              </div>

              <div className="mb-3">
                <p className="text-sm text-gray-500">Total Stock</p>
                <p className="font-semibold text-gray-700 dark:text-gray-200">
                  {product.variants?.reduce((sum, v) => sum + (v.stockQuantity || 0), 0) || 0}
                </p>
              </div>

              <div className="mt-6 flex gap-3">
                <NavLink to={`/app/products/${id}/edit`}>
                  <Button icon={EditIcon} size="small">
                    Edit Product
                  </Button>
                </NavLink>
              </div>
            </div>
          </div>
        </CardBody>
      </Card>

      {/* Product Variants Table */}
      <Card className="my-8 shadow-md">
        <CardBody>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-semibold text-gray-700 dark:text-gray-200">
              Product Variants
            </h2>
            <Button icon={AddIcon} size="small" onClick={openAddModal}>
              Add Variant
            </Button>
          </div>

          {variantsLoading ? (
            <div className="flex justify-center items-center py-12">
              <span>Loading variants...</span>
            </div>
          ) : (
            <>
              <TableContainer>
                <Table>
                  <TableHeader>
                    <tr>
                      <TableCell>ID</TableCell>
                      <TableCell>Image</TableCell>
                      <TableCell>Size</TableCell>
                      <TableCell>Color</TableCell>
                      <TableCell>Price</TableCell>
                      <TableCell>Stock</TableCell>
                      <TableCell>Actions</TableCell>
                    </tr>
                  </TableHeader>
                  <TableBody>
                    {variants.length > 0 ? (
                      variants.map((variant) => (
                        <TableRow key={variant.id}>
                          <TableCell>{variant.id}</TableCell>
                          <TableCell>
                            <Avatar
                              src={variant.image || "https://via.placeholder.com/50"}
                              alt="Variant"
                            />
                          </TableCell>
                          <TableCell>{variant.size || "-"}</TableCell>
                          <TableCell>{variant.color || "-"}</TableCell>
                          <TableCell>${variant.price?.toFixed(2) || "0.00"}</TableCell>
                          <TableCell>{variant.stockQuantity || 0}</TableCell>
                          <TableCell>
                            <div className="flex">
                              <Button
                                icon={EditIcon}
                                className="mr-2"
                                size="small"
                                layout="outline"
                                aria-label="Edit"
                                onClick={() => openEditModal(variant)}
                              />
                              <Button
                                icon={TrashIcon}
                                size="small"
                                layout="outline"
                                aria-label="Delete"
                                onClick={() => handleDeleteVariant(variant)}
                              />
                            </div>
                          </TableCell>
                        </TableRow>
                      ))
                    ) : (
                      <TableRow>
                        <TableCell colSpan="7" className="text-center">
                          No variants found
                        </TableCell>
                      </TableRow>
                    )}
                  </TableBody>
                </Table>
              </TableContainer>

              <TableFooter>
                <Pagination
                  totalResults={pagination.totalItems}
                  resultsPerPage={resultsPerPage}
                  label="Variants pagination"
                  onChange={(p) => setPage(p)}
                />
              </TableFooter>
            </>
          )}
        </CardBody>
      </Card>

      {/* Add/Edit Variant Modal */}
      <Modal isOpen={isModalOpen} onClose={closeModal}>
        <form onSubmit={handleSubmit}>
          <ModalHeader>
            {modalMode === "add" ? "Add Variant" : "Edit Variant"}
          </ModalHeader>
          <ModalBody>
            <Label className="mb-3">
              <span className="text-sm">Size (optional)</span>
              <Input
                type="text"
                value={size}
                onChange={(e) => setSize(e.target.value)}
                placeholder="e.g., M, L, XL"
                className="mt-1"
              />
            </Label>

            <Label className="mb-3">
              <span className="text-sm">Color (optional)</span>
              <Input
                type="text"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                placeholder="e.g., Red, Blue"
                className="mt-1"
              />
            </Label>

            <Label className="mb-3">
              <span className="text-sm">Price ($)</span>
              <Input
                type="number"
                step="0.01"
                min="0"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                placeholder="Enter price"
                className="mt-1"
              />
            </Label>

            <Label className="mb-3">
              <span className="text-sm">Stock Quantity</span>
              <Input
                type="number"
                min="0"
                value={stockQuantity}
                onChange={(e) => setStockQuantity(e.target.value)}
                placeholder="Enter stock quantity"
                className="mt-1"
              />
            </Label>

            <Label>
              <span className="text-sm">Variant Image (optional)</span>
              <input
                type="file"
                accept="image/*"
                onChange={handleVariantImageChange}
                className="mt-1 text-gray-800 dark:text-gray-300 text-sm"
              />
              {variantImagePreview && (
                <div className="mt-2">
                  <img
                    src={variantImagePreview}
                    alt="Preview"
                    className="w-20 h-20 object-cover rounded-lg border"
                  />
                </div>
              )}
            </Label>
          </ModalBody>
          <ModalFooter>
            <Button layout="outline" onClick={closeModal} disabled={submitting}>
              Cancel
            </Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? "Saving..." : modalMode === "add" ? "Add" : "Update"}
            </Button>
          </ModalFooter>
        </form>
      </Modal>
    </div>
  );
};

export default SingleProduct;
