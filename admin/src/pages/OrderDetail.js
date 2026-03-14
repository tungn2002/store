import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";
import PageTitle from "../components/Typography/PageTitle";
import { Button, Badge, TableContainer, Table, TableHeader, TableBody, TableCell, TableRow } from "@windmill/react-ui";
import { getOrderById } from "../utils/ordersApi";

function OrderDetail() {
  const { id } = useParams();
  const history = useHistory();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setLoading(true);
        const response = await getOrderById(parseInt(id));
        if (response) {
          setOrder(response);
        }
      } catch (error) {
        console.error("Failed to fetch order details:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [id]);

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

  // Format currency
  function formatCurrency(amount) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(amount || 0);
  }

  // Format date
  function formatDate(dateString) {
    if (!dateString) return "-";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN") + " " + date.toLocaleTimeString("vi-VN");
  }

  if (loading) {
    return (
      <div>
        <PageTitle>Chi tiết đơn hàng</PageTitle>
        <div className="text-center py-8">Đang tải...</div>
      </div>
    );
  }

  if (!order) {
    return (
      <div>
        <PageTitle>Order Not Found</PageTitle>
        <Button onClick={() => history.push("/app/orders")}>Back to Orders</Button>
      </div>
    );
  }

  return (
    <div>
      <PageTitle>Order Details #{order.id}</PageTitle>

      {/* Back Button */}
      <div className="mb-4">
        <Button onClick={() => history.push("/app/orders")}>
          ← Back to Orders
        </Button>
      </div>

      <div className="grid gap-6">
        {/* Order Info Card */}
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <h3 className="font-semibold text-lg mb-4 text-gray-700 dark:text-gray-300">Order Information</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Order ID
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">{order.id}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Status
              </p>
              <Badge type={getStatusBadgeType(order.status)}>
                {getStatusLabel(order.status)}
              </Badge>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Total Amount
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">{formatCurrency(order.totalAmount)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Customer Name
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">{order.customerName || "-"}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Customer Phone
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">{order.customerPhone || "-"}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Customer Email
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">{order.customerEmail || "-"}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Created At
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">
                {formatDate(order.createdAt)}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                Updated At
              </p>
              <p className="text-sm text-gray-700 dark:text-gray-300">
                {formatDate(order.updatedAt)}
              </p>
            </div>
          </div>
        </div>

        {/* Order Items Table */}
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <h3 className="font-semibold text-lg mb-4 text-gray-700 dark:text-gray-300">Order Items</h3>
          <TableContainer>
            <Table>
              <TableHeader>
                <tr>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">ID</TableCell>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">Product Name</TableCell>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">Size</TableCell>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">Color</TableCell>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">Quantity</TableCell>
                  <TableCell className="text-sm text-gray-600 dark:text-gray-400">Selling Price</TableCell>
                </tr>
              </TableHeader>
              <TableBody>
                {!order.items || order.items.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan="6" className="text-center text-sm text-gray-700 dark:text-gray-300">
                      Không có sản phẩm nào
                    </TableCell>
                  </TableRow>
                ) : (
                  order.items.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>#{item.id}</span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>{item.productName}</span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>{item.size || "-"}</span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>{item.color || "-"}</span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>{item.quantity}</span>
                      </TableCell>
                      <TableCell className="text-sm text-gray-700 dark:text-gray-300">
                        <span>
                          {formatCurrency(item.sellingPrice)}
                        </span>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </div>
    </div>
  );
}

export default OrderDetail;
