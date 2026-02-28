import React, { useState, useEffect } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Label, Input, Button } from "@windmill/react-ui";
import { getStoreSettings, updateStoreSettings } from "../utils/storeSettingsApi";
import { useToast } from "../utils/toast";

const Settings = () => {
  const toast = useToast();

  const [storeName, setStoreName] = useState("");
  const [address, setAddress] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  // Fetch store settings on mount
  useEffect(() => {
    const fetchStoreSettings = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          toast.error("Please login to view settings");
          return;
        }

        const data = await getStoreSettings(token);
        setStoreName(data.name || "");
        setAddress(data.address || "");
      } catch (err) {
        toast.error(
          err.response?.data?.message || "Failed to load store settings"
        );
      } finally {
        setIsLoading(false);
      }
    };

    fetchStoreSettings();
  }, [toast]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSaving(true);

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        toast.error("Please login again");
        return;
      }

      await updateStoreSettings(token, {
        name: storeName.trim(),
        address: address.trim(),
      });

      toast.success("Settings saved successfully!");
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "Failed to save settings";
      toast.error(errorMessage);

      // Handle specific validation errors from API
      if (err.response?.data?.errors) {
        const apiErrors = err.response.data.errors;
        apiErrors.forEach((error) => {
          toast.error(error.defaultMessage || error.message);
        });
      }
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <div className="px-6 py-8">
        <PageTitle>Store Settings</PageTitle>
        <div className="flex items-center justify-center mt-8">
          <div className="text-gray-600 dark:text-gray-300">Loading...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="px-6 py-8">
      <PageTitle>Store Settings</PageTitle>

      <div className="max-w-4xl mx-auto mt-6">
        <form onSubmit={handleSubmit}>
          {/* Store Information Card */}
          <div className="px-4 py-3 mb-8 bg-white rounded-lg shadow-md dark:bg-gray-800">
            <h3 className="mb-4 text-lg font-semibold text-gray-700 dark:text-gray-200">
              Store Information
            </h3>

            <div className="mb-4">
              <Label>
                <span>Store Name</span>
                <Input
                  className="mt-1"
                  type="text"
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  placeholder="Enter store name"
                  required
                />
              </Label>
            </div>

            <div className="mb-4">
              <Label>
                <span>Address</span>
                <Input
                  className="mt-1"
                  type="text"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  placeholder="Enter store address"
                />
              </Label>
            </div>
          </div>

          {/* Save Button */}
          <div className="flex justify-end">
            <Button type="submit" disabled={isSaving}>
              {isSaving ? "Saving..." : "Save Changes"}
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Settings;
