import React, { useState } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Label, Input, Button } from "@windmill/react-ui";

const Profile = () => {
  // Profile state
  const [name, setName] = useState("John Doe");
  const [email, setEmail] = useState("john@example.com");
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);
  const [profileSuccess, setProfileSuccess] = useState(false);

  // Password state
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);
  const [passwordSuccess, setPasswordSuccess] = useState(false);
  const [passwordError, setPasswordError] = useState("");

  const handleProfileUpdate = (e) => {
    e.preventDefault();
    setIsUpdatingProfile(true);
    setProfileSuccess(false);

    // Simulate API call
    setTimeout(() => {
      console.log("Profile Updated:", { name, email });
      setIsUpdatingProfile(false);
      setProfileSuccess(true);

      setTimeout(() => {
        setProfileSuccess(false);
      }, 3000);
    }, 1000);
  };

  const handlePasswordUpdate = (e) => {
    e.preventDefault();
    setPasswordError("");
    setPasswordSuccess(false);

    // Validate passwords
    if (newPassword !== confirmPassword) {
      setPasswordError("New passwords do not match");
      return;
    }

    if (newPassword.length < 6) {
      setPasswordError("Password must be at least 6 characters");
      return;
    }

    setIsUpdatingPassword(true);

    // Simulate API call
    setTimeout(() => {
      console.log("Password Updated:", { currentPassword, newPassword });
      setIsUpdatingPassword(false);
      setPasswordSuccess(true);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");

      setTimeout(() => {
        setPasswordSuccess(false);
      }, 3000);
    }, 1000);
  };

  return (
    <div className="px-6 py-8">
      <PageTitle>Manage Profile</PageTitle>

      <div className="max-w-4xl mx-auto mt-6">
        {/* Profile Information Card */}
        <div className="px-4 py-3 mb-8 bg-white rounded-lg shadow-md dark:bg-gray-800">
          <h3 className="mb-4 text-lg font-semibold text-gray-700 dark:text-gray-200">
            Profile Information
          </h3>

          <form onSubmit={handleProfileUpdate}>
            <div className="mb-4">
              <Label>
                <span>Name</span>
                <Input
                  className="mt-1"
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Enter your name"
                  required
                />
              </Label>
            </div>

            <div className="mb-4">
              <Label>
                <span>Email</span>
                <Input
                  className="mt-1"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter your email"
                  required
                />
              </Label>
            </div>

            {profileSuccess && (
              <div className="px-4 py-3 mb-4 text-green-700 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-100">
                Profile updated successfully!
              </div>
            )}

            <div className="flex justify-end">
              <Button type="submit" disabled={isUpdatingProfile}>
                {isUpdatingProfile ? "Updating..." : "Update Profile"}
              </Button>
            </div>
          </form>
        </div>

        {/* Change Password Card */}
        <div className="px-4 py-3 mb-8 bg-white rounded-lg shadow-md dark:bg-gray-800">
          <h3 className="mb-4 text-lg font-semibold text-gray-700 dark:text-gray-200">
            Change Password
          </h3>

          <form onSubmit={handlePasswordUpdate}>
            <div className="mb-4">
              <Label>
                <span>Current Password</span>
                <Input
                  className="mt-1"
                  type="password"
                  value={currentPassword}
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  placeholder="Enter current password"
                  required
                />
              </Label>
            </div>

            <div className="mb-4">
              <Label>
                <span>New Password</span>
                <Input
                  className="mt-1"
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter new password"
                  required
                />
              </Label>
            </div>

            <div className="mb-4">
              <Label>
                <span>Confirm New Password</span>
                <Input
                  className="mt-1"
                  type="password"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  placeholder="Confirm new password"
                  required
                />
              </Label>
            </div>

            {passwordError && (
              <div className="px-4 py-3 mb-4 text-red-700 bg-red-100 rounded-lg dark:bg-red-800 dark:text-red-100">
                {passwordError}
              </div>
            )}

            {passwordSuccess && (
              <div className="px-4 py-3 mb-4 text-green-700 bg-green-100 rounded-lg dark:bg-green-800 dark:text-green-100">
                Password updated successfully!
              </div>
            )}

            <div className="flex justify-end">
              <Button type="submit" disabled={isUpdatingPassword}>
                {isUpdatingPassword ? "Updating..." : "Update Password"}
              </Button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;
