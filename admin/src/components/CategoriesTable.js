import React, { useState, useEffect } from "react";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableFooter,
  Avatar,
  Pagination,
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Label,
  Input,
} from "@windmill/react-ui";
import { EditIcon, EyeIcon, TrashIcon, AddIcon } from "../icons";
import {
  getCategories,
  createCategory,
  updateCategory,
  deleteCategory,
} from "../utils/categoriesApi";
import { useToast } from "../utils/toast";

const CategoriesTable = ({ resultsPerPage }) => {
  const toast = useToast();
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    totalItems: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false,
  });

  // Modal states
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    image: null,
  });
  const [submitting, setSubmitting] = useState(false);

  // Fetch categories from API
  const fetchCategories = async () => {
    setLoading(true);
    try {
      const result = await getCategories(page - 1, resultsPerPage, "id");
      setData(result.items);
      setPagination({
        totalItems: result.totalItems,
        totalPages: result.totalPages,
        hasNext: result.hasNext,
        hasPrevious: result.hasPrevious,
      });
    } catch (error) {
      console.error("Error fetching categories:", error);
      toast.error("Error fetching categories");
      setData([]);
      setPagination({
        totalItems: 0,
        totalPages: 0,
        hasNext: false,
        hasPrevious: false,
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCategories();
  }, [page, resultsPerPage]);

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // Open add modal
  function openAddModal() {
    setFormData({ name: "", image: null });
    setIsAddModalOpen(true);
  }

  // Close add modal
  function closeAddModal() {
    setIsAddModalOpen(false);
    setFormData({ name: "", image: null });
  }

  // Open edit modal
  function openEditModal(category) {
    setSelectedCategory(category);
    setFormData({
      name: category.name || "",
      image: null,
    });
    setIsEditModalOpen(true);
  }

  // Close edit modal
  function closeEditModal() {
    setIsEditModalOpen(false);
    setSelectedCategory(null);
  }

  // Open delete modal
  function openDeleteModal(category) {
    setSelectedCategory(category);
    setIsDeleteModalOpen(true);
  }

  // Close delete modal
  function closeDeleteModal() {
    setIsDeleteModalOpen(false);
    setSelectedCategory(null);
  }

  // Handle form change
  function handleFormChange(e) {
    const { name, value, files } = e.target;
    if (name === "image" && files && files.length > 0) {
      setFormData((prev) => ({
        ...prev,
        [name]: files[0],
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  }

  // Handle add submit
  async function handleAddSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      const formDataToSend = new FormData();
      formDataToSend.append("name", formData.name);
      if (formData.image) {
        formDataToSend.append("image", formData.image);
      }

      await createCategory(formDataToSend);
      toast.success("Category created successfully");
      closeAddModal();
      fetchCategories();
    } catch (error) {
      console.error("Error creating category:", error);
      const errorMessage = error.response?.data?.message || "Failed to create category";
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  }

  // Handle edit submit
  async function handleEditSubmit(e) {
    e.preventDefault();
    setSubmitting(true);
    try {
      const formDataToSend = new FormData();
      formDataToSend.append("name", formData.name);
      if (formData.image) {
        formDataToSend.append("image", formData.image);
      }

      await updateCategory(selectedCategory.id, formDataToSend);
      toast.success("Category updated successfully");
      closeEditModal();
      fetchCategories();
    } catch (error) {
      console.error("Error updating category:", error);
      const errorMessage = error.response?.data?.message || "Failed to update category";
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  }

  // Handle delete submit
  async function handleDeleteSubmit() {
    setSubmitting(true);
    try {
      await deleteCategory(selectedCategory.id);
      toast.success("Category deleted successfully");
      closeDeleteModal();
      fetchCategories();
    } catch (error) {
      console.error("Error deleting category:", error);
      const errorMessage = error.response?.data?.message || "Failed to delete category";
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      {/* Add Modal */}
      <Modal isOpen={isAddModalOpen} onClose={closeAddModal}>
        <ModalHeader>Add Category</ModalHeader>
        <ModalBody>
          <form onSubmit={handleAddSubmit}>
            <div className="space-y-4">
              <div>
                <Label>Name</Label>
                <Input
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  placeholder="Enter category name"
                  required
                />
              </div>
              <div>
                <Label>Image</Label>
                <Input
                  name="image"
                  type="file"
                  accept="image/*"
                  onChange={handleFormChange}
                />
              </div>
            </div>
          </form>
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeAddModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleAddSubmit} disabled={submitting}>
              {submitting ? "Adding..." : "Add"}
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeAddModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleAddSubmit} disabled={submitting}>
              {submitting ? "Adding..." : "Add"}
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Edit Modal */}
      <Modal isOpen={isEditModalOpen} onClose={closeEditModal}>
        <ModalHeader>Edit Category</ModalHeader>
        <ModalBody>
          <form onSubmit={handleEditSubmit}>
            <div className="space-y-4">
              <div>
                <Label>Name</Label>
                <Input
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  placeholder="Enter category name"
                  required
                />
              </div>
              <div>
                <Label>Current Image</Label>
                {selectedCategory?.image && (
                  <Avatar src={selectedCategory.image} alt={selectedCategory.name} size="large" />
                )}
              </div>
              <div>
                <Label>Replace Image (optional)</Label>
                <Input
                  name="image"
                  type="file"
                  accept="image/*"
                  onChange={handleFormChange}
                />
              </div>
            </div>
          </form>
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeEditModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleEditSubmit} disabled={submitting}>
              {submitting ? "Saving..." : "Save Changes"}
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeEditModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleEditSubmit} disabled={submitting}>
              {submitting ? "Saving..." : "Save Changes"}
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Delete Modal */}
      <Modal isOpen={isDeleteModalOpen} onClose={closeDeleteModal}>
        <ModalHeader className="flex items-center">
          <TrashIcon className="w-6 h-6 mr-3" />
          Delete Category
        </ModalHeader>
        <ModalBody>
          Are you sure you want to delete category{" "}
          {selectedCategory && `"${selectedCategory.name}"`}
          ?
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeDeleteModal}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleDeleteSubmit}>Delete</Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeDeleteModal}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleDeleteSubmit}>
              Delete
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Add Button */}
      <div className="mb-4">
        <Button onClick={openAddModal}>
          <span className="flex items-center">
            <AddIcon className="w-5 h-5 mr-2" />
            Add Category
          </span>
        </Button>
      </div>

      {/* Table */}
      <TableContainer className="mb-8">
        {loading ? (
          <div className="flex justify-center items-center py-8">
            <span>Loading...</span>
          </div>
        ) : (
          <Table>
            <TableHeader>
              <tr>
                <TableCell>ID</TableCell>
                <TableCell>Image</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Action</TableCell>
              </tr>
            </TableHeader>
            <TableBody>
              {data.length > 0 ? (
                data.map((category) => (
                  <TableRow key={category.id}>
                    <TableCell>
                      <span className="text-sm">{category.id}</span>
                    </TableCell>
                    <TableCell>
                      <Avatar
                        className="mr-3"
                        src={category.image}
                        alt={category.name}
                      />
                    </TableCell>
                    <TableCell>
                      <span className="font-medium">{category.name}</span>
                    </TableCell>
                    <TableCell>
                      <div className="flex">
                        <Button
                          icon={EditIcon}
                          className="mr-3"
                          layout="outline"
                          aria-label="Edit"
                          size="small"
                          onClick={() => openEditModal(category)}
                        />
                        <Button
                          icon={TrashIcon}
                          layout="outline"
                          aria-label="Delete"
                          size="small"
                          onClick={() => openDeleteModal(category)}
                        />
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan="4" className="text-center">
                    No categories found
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
        <TableFooter>
          <Pagination
            totalResults={pagination.totalItems}
            resultsPerPage={resultsPerPage}
            label="Table navigation"
            onChange={onPageChange}
          />
        </TableFooter>
      </TableContainer>
    </div>
  );
};

export default CategoriesTable;
