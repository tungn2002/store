import React, { useState, useEffect } from "react";
import { useParams, useHistory } from "react-router-dom";
import PageTitle from "../components/Typography/PageTitle";
import {
  TableBody,
  TableContainer,
  Table,
  TableHeader,
  TableCell,
  TableRow,
  Button,
} from "@windmill/react-ui";

// Sample permissions data
const permissionsData = [
  { id: 1, name: "view_dashboard", display_name: "View Dashboard", active: true },
  { id: 2, name: "create_product", display_name: "Create Product", active: true },
  { id: 3, name: "edit_product", display_name: "Edit Product", active: true },
  { id: 4, name: "delete_product", display_name: "Delete Product", active: false },
  { id: 5, name: "view_orders", display_name: "View Orders", active: true },
  { id: 6, name: "manage_orders", display_name: "Manage Orders", active: true },
  { id: 7, name: "view_customers", display_name: "View Customers", active: true },
  { id: 8, name: "manage_customers", display_name: "Manage Customers", active: false },
  { id: 9, name: "manage_roles", display_name: "Manage Roles", active: false },
  { id: 10, name: "manage_settings", display_name: "Manage Settings", active: true },
];

// Sample role data
const rolesData = {
  1: { id: 1, name: "admin", display_name: "Administrator" },
  2: { id: 2, name: "user", display_name: "User" },
};

// Toggle Switch Component
const ToggleSwitch = ({ active, disabled }) => {
  return (
    <div
      className={`relative inline-flex items-center h-5 rounded-full w-10 transition-colors ${
        active ? "bg-purple-600" : "bg-gray-300 dark:bg-gray-600"
      } ${disabled ? "opacity-50 cursor-not-allowed" : "cursor-pointer"}`}
    >
      <span
        className={`inline-block w-3 h-3 bg-white rounded-full transition-transform transform ${
          active ? "translate-x-5" : "translate-x-1"
        }`}
      />
    </div>
  );
};

const RolePermissions = () => {
  const { id } = useParams();
  const history = useHistory();
  const [role, setRole] = useState(null);
  const [permissions, setPermissions] = useState([]);

  useEffect(() => {
    // Load role data
    const roleData = rolesData[id];
    if (roleData) {
      setRole(roleData);
    }

    // Load permissions data
    setPermissions(permissionsData);
  }, [id]);

  if (!role) {
    return (
      <div>
        <PageTitle>Role Not Found</PageTitle>
        <p className="mt-4">The requested role does not exist.</p>
        <Button className="mt-4" onClick={() => history.push("/app/roles")}>
          Back to Roles
        </Button>
      </div>
    );
  }

  return (
    <div>
      <PageTitle>Permissions - {role.display_name}</PageTitle>

      <div className="mt-6 mb-4">
        <p className="text-gray-600 dark:text-gray-400">
          Manage permissions for <span className="font-semibold">{role.name}</span> role.
        </p>
      </div>

      <TableContainer className="mb-8">
        <Table>
          <TableHeader>
            <tr>
              <TableCell>ID</TableCell>
              <TableCell>Permission Name</TableCell>
              <TableCell>Display Name</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Toggle</TableCell>
            </tr>
          </TableHeader>
          <TableBody>
            {permissions.map((permission) => (
              <TableRow key={permission.id}>
                <TableCell>
                  <span className="text-sm">{permission.id}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm font-medium">{permission.name}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{permission.display_name}</span>
                </TableCell>
                <TableCell>
                  <span
                    className={`text-sm ${
                      permission.active
                        ? "text-green-600 dark:text-green-400"
                        : "text-gray-500 dark:text-gray-400"
                    }`}
                  >
                    {permission.active ? "Active" : "Inactive"}
                  </span>
                </TableCell>
                <TableCell>
                  <ToggleSwitch active={permission.active} disabled={true} />
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <div className="flex justify-end">
        <Button onClick={() => history.push("/app/roles")}>Back to Roles</Button>
      </div>
    </div>
  );
};

export default RolePermissions;
