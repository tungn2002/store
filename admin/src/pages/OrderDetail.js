import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";
import PageTitle from "../components/Typography/PageTitle";
import { Button, Badge, TableContainer, Table, TableHeader, TableBody, TableCell, TableRow } from "@windmill/react-ui";
import response from "../utils/demo/ordersData";

function OrderDetail() {
  const { id } = useParams();
  const history = useHistory();
  const [order, setOrder] = useState(null);

  useEffect(() => {
    const foundOrder = response.find((o) => o.id === parseInt(id));
    if (foundOrder) {
      setOrder(foundOrder);
    }
  }, [id]);

  // Get status badge type
  function getStatusBadgeType(status) {
    if (status === "pending") return "warning";
    if (status === "completed") return "success";
    return "neutral";
  }

  // Get status label
  function getStatusLabel(status) {
    if (status === "pending") return "Đang chờ";
    if (status === "completed") return "Hoàn thành";
    return status;
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
          <h3 className="font-semibold text-lg mb-4">Order Information</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Order ID
              </p>
              <p className="font-semibold">#{order.id}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Status
              </p>
              <Badge type={getStatusBadgeType(order.status)}>
                {getStatusLabel(order.status)}
              </Badge>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Total Amount
              </p>
              <p className="font-semibold">${order.total_amount.toFixed(2)}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                User ID
              </p>
              <p className="font-semibold">#{order.user_id}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Note
              </p>
              <p className="font-semibold">
                {order.note || "No note"}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Created At
              </p>
              <p className="font-semibold">
                {new Date(order.created_at).toLocaleString()}
              </p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Updated At
              </p>
              <p className="font-semibold">
                {new Date(order.updated_at).toLocaleString()}
              </p>
            </div>
          </div>
        </div>

        {/* Customer Info Card */}
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <h3 className="font-semibold text-lg mb-4">Customer Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Name
              </p>
              <p className="font-semibold">{order.customer.name}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Phone
              </p>
              <p className="font-semibold">{order.customer.phone}</p>
            </div>
            <div>
              <p className="text-sm text-gray-500 dark:text-gray-400">
                Email
              </p>
              <p className="font-semibold">{order.customer.email}</p>
            </div>
          </div>
        </div>

        {/* Order Items Table */}
        <div className="p-4 bg-white rounded-lg shadow-xs dark:bg-gray-800">
          <h3 className="font-semibold text-lg mb-4">Order Items</h3>
          <TableContainer>
            <Table>
              <TableHeader>
                <tr>
                  <TableCell>ID</TableCell>
                  <TableCell>Product Name</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>Size</TableCell>
                  <TableCell>Color</TableCell>
                  <TableCell>Quantity</TableCell>
                  <TableCell>Selling Price</TableCell>
                </tr>
              </TableHeader>
              <TableBody>
                {order.order_items.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell>
                      <span className="text-sm">#{item.id}</span>
                    </TableCell>
                    <TableCell>
                      <span className="font-medium">{item.product_name}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{item.sku}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{item.size}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{item.color}</span>
                    </TableCell>
                    <TableCell>
                      <span className="text-sm">{item.quantity}</span>
                    </TableCell>
                    <TableCell>
                      <span className="font-medium">
                        ${item.selling_price.toFixed(2)}
                      </span>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
      </div>
    </div>
  );
}

export default OrderDetail;
