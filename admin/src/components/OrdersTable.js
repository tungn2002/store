import React, { useState, useEffect } from "react";
import { useHistory } from "react-router-dom";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  TableFooter,
  Badge,
  Pagination,
  Button,
} from "@windmill/react-ui";
import { EyeIcon } from "../icons";
import { getAllOrders } from "../utils/ordersApi";

const OrdersTable = ({ resultsPerPage, filter }) => {
  const history = useHistory();
  const [page, setPage] = useState(1);
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [totalResults, setTotalResults] = useState(0);

  // Fetch orders from API
  const fetchOrders = async () => {
    try {
      setLoading(true);
      // API uses 0-based page index
      const response = await getAllOrders(page - 1, resultsPerPage);
      if (response) {
        setData(response.items || []);
        setTotalResults(response.totalItems || 0);
      }
    } catch (error) {
      console.error("Failed to fetch orders:", error);
    } finally {
      setLoading(false);
    }
  };

  // pagination change control
  function onPageChange(p) {
    setPage(p);
  }

  // on page change or resultsPerPage change, fetch new data
  useEffect(() => {
    fetchOrders();
  }, [page, resultsPerPage, filter]);

  // Get status badge type
  function getStatusBadgeType(status) {
    if (status === "PENDING") return "warning";
    if (status === "PAID") return "success";
    if (status === "CANCEL") return "neutral";
    return "neutral";
  }

  // Get status label
  function getStatusLabel(status) {
    if (status === "PENDING") return "Đang chờ";
    if (status === "PAID") return "Hoàn thành";
    if (status === "CANCEL") return "Đã hủy";
    return status;
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN") + " " + date.toLocaleTimeString("vi-VN");
  }

  if (loading) {
    return <div className="text-center py-8">Đang tải đơn hàng...</div>;
  }

  return (
    <div>
      {/* Table */}
      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">ID</TableCell>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">Customer Email</TableCell>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">Total Amount</TableCell>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">Created At</TableCell>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">Status</TableCell>
              <TableCell className="text-sm text-gray-600 dark:text-gray-400">Action</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {data.length === 0 ? (
              <TableRow>
                <TableCell colSpan="6" className="text-center text-sm text-gray-700 dark:text-gray-300">
                  Không có đơn hàng nào
                </TableCell>
              </TableRow>
            ) : (
              data.map((order) => (
                <TableRow key={order.id}>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <span>#{order.id}</span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <span>{order.customerEmail || "-"}</span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <span>
                      {new Intl.NumberFormat("vi-VN", {
                        style: "currency",
                        currency: "VND",
                      }).format(order.totalAmount || 0)}
                    </span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <span>{formatDate(order.createdAt)}</span>
                  </TableCell>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <Badge type={getStatusBadgeType(order.status)}>
                      {getStatusLabel(order.status)}
                    </Badge>
                  </TableCell>
                  <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                    <Button
                      icon={EyeIcon}
                      aria-label="View Details"
                      size="small"
                      onClick={() => history.push(`/app/order/${order.id}`)}
                    />
                  </TableCell>
                </TableRow>
              ))
            )}
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

export default OrdersTable;
