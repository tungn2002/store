import React, { useState, useEffect } from "react";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableFooter,
  Pagination,
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Label,
  Input,
} from "@windmill/react-ui";
import { EditIcon, TrashIcon, AddIcon } from "../icons";
import response from "../utils/demo/brandsData";

const BrandsTable = ({ resultsPerPage }) => {
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);

  // Modal states
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedBrand, setSelectedBrand] = useState(null);
  const [formData, setFormData] = useState({
    name: "",
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
    setFormData({ name: "" });
    setIsAddModalOpen(true);
  }

  // Close add modal
  function closeAddModal() {
    setIsAddModalOpen(false);
    setFormData({ name: "" });
  }

  // Open edit modal
  function openEditModal(brand) {
    setSelectedBrand(brand);
    setFormData({
      name: brand.name || "",
    });
    setIsEditModalOpen(true);
  }

  // Close edit modal
  function closeEditModal() {
    setIsEditModalOpen(false);
    setSelectedBrand(null);
  }

  // Open delete modal
  function openDeleteModal(brand) {
    setSelectedBrand(brand);
    setIsDeleteModalOpen(true);
  }

  // Close delete modal
  function closeDeleteModal() {
    setIsDeleteModalOpen(false);
    setSelectedBrand(null);
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
    console.log("Added brand:", formData);
    closeAddModal();
  }

  // Handle edit submit
  function handleEditSubmit(e) {
    e.preventDefault();
    console.log("Updated brand:", formData);
    closeEditModal();
  }

  // Handle delete submit
  function handleDeleteSubmit() {
    console.log("Deleted brand:", selectedBrand);
    closeDeleteModal();
  }

  return (
    <div>
      {/* Add Modal */}
      <Modal isOpen={isAddModalOpen} onClose={closeAddModal}>
        <ModalHeader>Add Brand</ModalHeader>
        <ModalBody>
          <form onSubmit={handleAddSubmit}>
            <div className="space-y-4">
              <div>
                <Label>Name</Label>
                <Input
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  placeholder="Enter brand name"
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
        <ModalHeader>Edit Brand</ModalHeader>
        <ModalBody>
          <form onSubmit={handleEditSubmit}>
            <div className="space-y-4">
              <div>
                <Label>Name</Label>
                <Input
                  name="name"
                  value={formData.name}
                  onChange={handleFormChange}
                  placeholder="Enter brand name"
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
          Delete Brand
        </ModalHeader>
        <ModalBody>
          Are you sure you want to delete brand{" "}
          {selectedBrand && `"${selectedBrand.name}"`}
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
            Add Brand
          </span>
        </Button>
      </div>

      {/* Table */}
      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell>ID</TableCell>
              <TableCell>Name</TableCell>
              <TableCell>Action</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {data.map((brand) => (
              <TableRow key={brand.id}>
                <TableCell>
                  <span className="text-sm">{brand.id}</span>
                </TableCell>
                <TableCell>
                  <span className="font-medium">{brand.name}</span>
                </TableCell>
                <TableCell>
                  <div className="flex">
                    <Button
                      icon={EditIcon}
                      className="mr-3"
                      layout="outline"
                      aria-label="Edit"
                      size="small"
                      onClick={() => openEditModal(brand)}
                    />
                    <Button
                      icon={TrashIcon}
                      layout="outline"
                      aria-label="Delete"
                      size="small"
                      onClick={() => openDeleteModal(brand)}
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

export default BrandsTable;
