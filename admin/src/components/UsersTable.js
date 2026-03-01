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
} from "@windmill/react-ui";
import { TrashIcon } from "../icons";
import {
  getUsers,
  deleteUser,
} from "../utils/usersApi";
import { useToast } from "../utils/toast";

const UsersTable = ({ resultsPerPage, filter }) => {
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
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // Fetch users from API
  const fetchUsers = async () => {
    setLoading(true);
    try {
      const result = await getUsers(page - 1, resultsPerPage, "createdAt", filter);
      setData(result.items);
      setPagination({
        totalItems: result.totalItems,
        totalPages: result.totalPages,
        hasNext: result.hasNext,
        hasPrevious: result.hasPrevious,
      });
    } catch (error) {
      console.error("Error fetching users:", error);
      toast.error("Error fetching users");
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
    fetchUsers();
  }, [page, resultsPerPage, filter]);

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

  // Handle delete submit
  async function handleDeleteSubmit() {
    setSubmitting(true);
    try {
      await deleteUser(selectedUser.id);
      toast.success("User deleted successfully");
      closeDeleteModal();
      fetchUsers();
    } catch (error) {
      console.error("Error deleting user:", error);
      const errorMessage = error.response?.data?.message || "Failed to delete user";
      toast.error(errorMessage);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      {/* Delete Modal */}
      <Modal isOpen={isDeleteModalOpen} onClose={closeDeleteModal}>
        <ModalHeader className="flex items-center">
          <TrashIcon className="w-6 h-6 mr-3" />
          Delete User
        </ModalHeader>
        <ModalBody>
          Are you sure you want to delete user{" "}
          {selectedUser && `"${selectedUser.name}"`}
          ?
        </ModalBody>
        <ModalFooter>
          <div className="hidden sm:block">
            <Button layout="outline" onClick={closeDeleteModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="hidden sm:block">
            <Button onClick={handleDeleteSubmit} disabled={submitting}>
              {submitting ? "Deleting..." : "Delete"}
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" layout="outline" onClick={closeDeleteModal} disabled={submitting}>
              Cancel
            </Button>
          </div>
          <div className="block w-full sm:hidden">
            <Button block size="large" onClick={handleDeleteSubmit} disabled={submitting}>
              {submitting ? "Deleting..." : "Delete"}
            </Button>
          </div>
        </ModalFooter>
      </Modal>

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
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Phone Number</TableCell>
                <TableCell>Gender</TableCell>
                <TableCell>Date of Birth</TableCell>
                <TableCell>Address</TableCell>
                <TableCell>Action</TableCell>
              </tr>
            </TableHeader>
            <TableBody>
              {data.length > 0 ? (
                data.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>
                      <div className="flex items-center text-sm">
                        <div>
                          <p className="font-semibold">{user.name}</p>
                        </div>
                      </div>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{user.email}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{user.phoneNumber || "N/A"}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{user.gender || "N/A"}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{user.dateOfBirth || "N/A"}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm max-w-xs truncate block" title={user.address}>
                        {user.address || "N/A"}
                      </span>
                    </TableCell>
                    <TableCell>
                      <div className="flex">
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
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan="7" className="text-center">
                    {filter ? "No users found matching your search" : "No users found"}
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

export default UsersTable;
