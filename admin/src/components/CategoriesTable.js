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
import response from "../utils/demo/categoriesData";

const CategoriesTable = ({ resultsPerPage }) => {
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);

  // Modal states
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
    image: "",
  });

  // pagination setup
  const totalResults = response.length;

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // on page change, load new sliced data
  useEffect(() => {
    setData(
      response.slice((page - 1) * resultsPerPage, page * resultsPerPage)
    );
  }, [page, resultsPerPage]);

  // Open add modal
  function openAddModal() {
    setFormData({ name: "", image: "" });
    setIsAddModalOpen(true);
  }

  // Close add modal
  function closeAddModal() {
    setIsAddModalOpen(false);
    setFormData({ name: "", image: "" });
  }

  // Open edit modal
  function openEditModal(category) {
    setSelectedCategory(category);
    setFormData({
      name: category.name || "",
      image: category.image || "",
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
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  // Handle add submit
  function handleAddSubmit(e) {
    e.preventDefault();
    console.log("Added category:", formData);
    closeAddModal();
  }

  // Handle edit submit
  function handleEditSubmit(e) {
    e.preventDefault();
    console.log("Updated category:", formData);
    closeEditModal();
  }

  // Handle delete submit
  function handleDeleteSubmit() {
    console.log("Deleted category:", selectedCategory);
    closeDeleteModal();
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
                />
              </div>
              <div>
                <Label>Image URL</Label>
                <Input
                  name="image"
                  value={formData.image}
                  onChange={handleFormChange}
                  placeholder="Enter image URL"
                />
              </div>
            </div>
          </form>
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeAddModal}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleAddSubmit}>Add</Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeAddModal}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleAddSubmit}>
              Add
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
                />
              </div>
              <div>
                <Label>Image URL</Label>
                <Input
                  name="image"
                  value={formData.image}
                  onChange={handleFormChange}
                  placeholder="Enter image URL"
                />
              </div>
            </div>
          </form>
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeEditModal}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleEditSubmit}>Save Changes</Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeEditModal}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleEditSubmit}>
              Save Changes
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
            {data.map((category) => (
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
            ))}
          </TableBody>
        </Table>
        <TableFooter>
          <Pagination
            totalResults={totalResults}
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
