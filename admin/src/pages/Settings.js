import React, { useState } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Label, Input, Button } from "@windmill/react-ui";

const Settings = () => {
  const [storeName, setStoreName] = useState("My Store");
  const [address, setAddress] = useState("123 Main Street, New York, NY 10001");
  const [isSaving, setIsSaving] = useState(false);
  const [saveSuccess, setSaveSuccess] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsSaving(true);
    setSaveSuccess(false);

    // Simulate API call
    setTimeout(() => {
      console.log("Store Settings Updated:", {
        storeName,
        address,
      });
      setIsSaving(false);
      setSaveSuccess(true);

      // Hide success message after 3 seconds
      setTimeout(() => {
        setSaveSuccess(false);
      }, 3000);
    }, 1000);
  };

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
                  required
                />
              </Label>
            </div>
          </div>

          {/* Success Message */}
          {saveSuccess && (
            <div className="px-4 py-3 mb-4 text-green-700 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-100">
              Settings saved successfully!
            </div>
          )}

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
