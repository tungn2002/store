import React, { useState, useEffect } from "react";
import { NavLink, useHistory, useParams } from "react-router-dom";
import Icon from "../components/Icon";
import PageTitle from "../components/Typography/PageTitle";
import { HomeIcon, EditIcon } from "../icons";
import {
  Card,
  CardBody,
  Label,
  Input,
  Textarea,
  Button,
  Select,
} from "@windmill/react-ui";
import { useToast } from "../utils/toast";
import { getProduct, updateProduct, getCategories, getBrands } from "../utils/productsApi";

const FormTitle = ({ children }) => {
  return (
    <h2 className="mb-3 text-sm font-semibold text-gray-600 dark:text-gray-300">
      {children}
    </h2>
  );
};

const EditProduct = () => {
  const { id } = useParams();
  const history = useHistory();
  const toast = useToast();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [image, setImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [categoryId, setCategoryId] = useState("");
  const [brandId, setBrandId] = useState("");

  const [categories, setCategories] = useState([]);
  const [brands, setBrands] = useState([]);

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadCategoriesAndBrands();
    loadProduct();
  }, [id]);

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
      toast.error("Failed to load categories or brands");
    }
  };

  const loadProduct = async () => {
    try {
      const product = await getProduct(id);
      setName(product.name || "");
      setDescription(product.description || "");
      setImagePreview(product.image || null);
      setCategoryId(product.category?.id || "");
      setBrandId(product.brand?.id || "");
    } catch (error) {
      console.error("Error loading product:", error);
      toast.error("Failed to load product");
    } finally {
      setLoading(false);
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setImage(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setImagePreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!name.trim()) {
      toast.error("Product name is required");
      return;
    }
    if (!categoryId) {
      toast.error("Please select a category");
      return;
    }
    if (!brandId) {
      toast.error("Please select a brand");
      return;
    }

    setSubmitting(true);

    try {
      const formData = new FormData();
      formData.append("name", name);
      formData.append("description", description);
      formData.append("categoryId", categoryId);
      formData.append("brandId", brandId);
      if (image) {
        formData.append("image", image);
      }

      await updateProduct(id, formData);
      toast.success("Product updated successfully");
      history.push("/app/all-products");
    } catch (error) {
      console.error("Error updating product:", error);
      const message = error.response?.data?.message || "Failed to update product";
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center py-12">
        <span>Loading...</span>
      </div>
    );
  }

  return (
    <div>
      <PageTitle>Edit Product</PageTitle>

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
        <p className="text-gray-600 dark:text-gray-300">Edit Product</p>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="w-full mt-8 grid gap-4 grid-col md:grid-cols-3">
          <Card className="row-span-2 md:col-span-2">
            <CardBody>
              <FormTitle>Product Image</FormTitle>
              <div className="mb-4">
                <input
                  type="file"
                  accept="image/*"
                  onChange={handleImageChange}
                  className="text-gray-800 dark:text-gray-300"
                />
                {imagePreview && (
                  <div className="mt-3">
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="w-32 h-32 object-cover rounded-lg border"
                    />
                  </div>
                )}
              </div>

              <FormTitle>Product Name</FormTitle>
              <Label>
                <Input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Type product name here"
                  className="mb-4"
                />
              </Label>

              <FormTitle>Description</FormTitle>
              <Label>
                <Textarea
                  rows="5"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Enter product description here"
                  className="mb-4"
                />
              </Label>

              <div className="w-full mt-6">
                <Button 
                  type="submit" 
                  size="large" 
                  iconLeft={EditIcon}
                  disabled={submitting}
                >
                  {submitting ? "Updating..." : "Update Product"}
                </Button>
              </div>
            </CardBody>
          </Card>

          <Card className="h-auto">
            <CardBody>
              <Label className="mt-4">
                <FormTitle>Select Product Category</FormTitle>
                <Select 
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  className="mt-1"
                >
                  <option value="">-- Select Category --</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </Select>
              </Label>

              <Label className="mt-4">
                <FormTitle>Select Product Brand</FormTitle>
                <Select 
                  value={brandId}
                  onChange={(e) => setBrandId(e.target.value)}
                  className="mt-1"
                >
                  <option value="">-- Select Brand --</option>
                  {brands.map((brand) => (
                    <option key={brand.id} value={brand.id}>
                      {brand.name}
                    </option>
                  ))}
                </Select>
              </Label>
            </CardBody>
          </Card>
        </div>
      </form>
    </div>
  );
};

export default EditProduct;
