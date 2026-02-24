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
  Badge,
  Pagination,
  Button,
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Label,
  Input,
  Select,
} from "@windmill/react-ui";
import { EditIcon, EyeIcon, TrashIcon } from "../icons";
import response from "../utils/demo/usersData";

const UsersTable = ({ resultsPerPage, filter }) => {
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);

  // Modal states
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [editFormData, setEditFormData] = useState({
    name: "",
    email: "",
    phone: "",
    dob: "",
    gender: "",
    address: "",
  });

  // Filter data by email
  const filteredData = filter
    ? response.filter((user) =>
        user.email.toLowerCase().includes(filter.toLowerCase())
      )
    : response;

  // pagination setup
  const totalResults = filteredData.length;

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // on page change, load new sliced data
  useEffect(() => {
    setData(
      filteredData.slice((page - 1) * resultsPerPage, page * resultsPerPage)
    );
  }, [page, resultsPerPage, filter, filteredData]);

  // Open detail modal
  function openDetailModal(user) {
    setSelectedUser(user);
    setIsDetailModalOpen(true);
  }

  // Close detail modal
  function closeDetailModal() {
    setIsDetailModalOpen(false);
    setSelectedUser(null);
  }

  // Open delete modal
  function openDeleteModal(user) {
    setSelectedUser(user);
    setIsDeleteModalOpen(true);
  }

  // Close delete modal
  function closeDeleteModal() {
    setIsDeleteModalOpen(false);
    setSelectedUser(null);
  }

  // Open edit modal
  function openEditModal(user) {
    setSelectedUser(user);
    setEditFormData({
      name: user.name || "",
      email: user.email || "",
      phone: user.phone || "",
      dob: user.dob || "",
      gender: user.gender || "",
      address: user.address || "",
    });
    setIsEditModalOpen(true);
  }

  // Close edit modal
  function closeEditModal() {
    setIsEditModalOpen(false);
    setSelectedUser(null);
  }

  // Handle edit form change
  function handleEditFormChange(e) {
    const { name, value } = e.target;
    setEditFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  // Handle edit form submit
  function handleEditSubmit(e) {
    e.preventDefault();
    // Here you would typically make an API call to update the user
    console.log("Updated user data:", editFormData);
    closeEditModal();
  }

  return (
    <div>
      {/* Detail Modal */}
      <Modal isOpen={isDetailModalOpen} onClose={closeDetailModal}>
        <ModalHeader>Customer Details</ModalHeader>
        <ModalBody>
          {selectedUser && (
            <div className="space-y-3">
              <div className="flex items-center">
                <Avatar
                  className="mr-4"
                  src={selectedUser.avatar}
                  alt="User image"
                />
                <div>
                  <p className="font-semibold text-lg">{selectedUser.name}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    {selectedUser.email}
                  </p>
                </div>
              </div>
              <hr className="dark:border-gray-600" />
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Phone Number
                  </p>
                  <p className="font-medium">{selectedUser.phone}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Date of Birth
                  </p>
                  <p className="font-medium">{selectedUser.dob}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Gender
                  </p>
                  <p className="font-medium">{selectedUser.gender}</p>
                </div>
                <div>
                  <p className="text-sm text-gray-500 dark:text-gray-400">
                    Joined On
                  </p>
                  <p className="font-medium">
                    {new Date(selectedUser.joined_on).toLocaleDateString()}
                  </p>
                </div>
              </div>
              <div>
                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Address
                </p>
                <p className="font-medium">{selectedUser.address}</p>
              </div>
            </div>
          )}
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeDetailModal}>
              Close
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeDetailModal}>
              Close
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Delete Modal */}
      <Modal isOpen={isDeleteModalOpen} onClose={closeDeleteModal}>
        <ModalHeader className="flex items-center">
          <TrashIcon className="w-6 h-6 mr-3" />
          Delete Customer
        </ModalHeader>
        <ModalBody>
          Are you sure you want to delete customer{" "}
          {selectedUser && `"${selectedUser.name}"`}
          ?
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeDeleteModal}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button>Delete</Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeDeleteModal}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large">
              Delete
            </Button>
          </div>
        </ModalFooter>
      </Modal>

      {/* Edit Modal */}
      <Modal isOpen={isEditModalOpen} onClose={closeEditModal}>
        <ModalHeader>Edit Customer</ModalHeader>
        <ModalBody>
          <form onSubmit={handleEditSubmit}>
            <div className="space-y-4">
              <div>
                <Label>Name</Label>
                <Input
                  name="name"
                  value={editFormData.name}
                  onChange={handleEditFormChange}
                  placeholder="Enter name"
                />
              </div>
              <div>
                <Label>Email</Label>
                <Input
                  name="email"
                  type="email"
                  value={editFormData.email}
                  onChange={handleEditFormChange}
                  placeholder="Enter email"
                />
              </div>
              <div>
                <Label>Phone Number</Label>
                <Input
                  name="phone"
                  value={editFormData.phone}
                  onChange={handleEditFormChange}
                  placeholder="Enter phone number"
                />
              </div>
              <div>
                <Label>Date of Birth</Label>
                <Input
                  name="dob"
                  type="date"
                  value={editFormData.dob}
                  onChange={handleEditFormChange}
                />
              </div>
              <div>
                <Label>Gender</Label>
                <Select
                  name="gender"
                  value={editFormData.gender}
                  onChange={handleEditFormChange}
                >
                  <option value="" disabled>
                    Select gender
                  </option>
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </Select>
              </div>
              <div>
                <Label>Address</Label>
                <Input
                  name="address"
                  value={editFormData.address}
                  onChange={handleEditFormChange}
                  placeholder="Enter address"
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
            <Button type="submit" onClick={handleEditSubmit}>
              Save Changes
            </Button>
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

      {/* Table */}
      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Phone Number</TableCell>
              <TableCell>Action</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {data.map((user) => (
              <TableRow key={user.id}>
                <TableCell>
                  <div className="flex items-center text-sm">
                    <Avatar
                      className="hidden mr-3 md:block"
                      src={user.avatar}
                      alt="User image"
                    />
                    <div>
                      <p className="font-semibold">{user.name}</p>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{user.email}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{user.phone}</span>
                </TableCell>
                <TableCell>
                  <div className="flex">
                    <Button
                      icon={EyeIcon}
                      className="mr-3"
                      aria-label="View Details"
                      size="small"
                      onClick={() => openDetailModal(user)}
                    />
                    <Button
                      icon={EditIcon}
                      className="mr-3"
                      layout="outline"
                      aria-label="Edit"
                      size="small"
                      onClick={() => openEditModal(user)}
                    />
                    <Button
                      icon={TrashIcon}
                      layout="outline"
                      aria-label="Delete"
                      size="small"
                      onClick={() => openDeleteModal(user)}
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

export default UsersTable;
