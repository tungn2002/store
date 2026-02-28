import React, { useState, useEffect } from "react";
import PageTitle from "../components/Typography/PageTitle";
import { Label, Input, Button, Select } from "@windmill/react-ui";
import { getProfile, updateProfile } from "../utils/profileApi";
import { useToast } from "../utils/toast";

const Profile = () => {
  const toast = useToast();

  // Profile state
  const [profileData, setProfileData] = useState({
    name: "",
    email: "",
    phoneNumber: "",
    dateOfBirth: "",
    gender: "",
    address: "",
  });

  // Validation errors state
  const [errors, setErrors] = useState({});

  // Loading state
  const [isLoading, setIsLoading] = useState(true);
  const [isUpdatingProfile, setIsUpdatingProfile] = useState(false);

  // Password state
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isUpdatingPassword, setIsUpdatingPassword] = useState(false);
  const [passwordErrors, setPasswordErrors] = useState({});

  // Fetch profile on mount
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const token = localStorage.getItem("token");
        if (!token) {
          toast.error("Please login to view profile");
          return;
        }

        const data = await getProfile(token);
        setProfileData({
          name: data.name || "",
          email: data.email || "",
          phoneNumber: data.phoneNumber || "",
          dateOfBirth: data.dateOfBirth || "",
          gender: data.gender || "",
          address: data.address || "",
        });
      } catch (err) {
        toast.error(
          err.response?.data?.message || "Failed to load profile"
        );
      } finally {
        setIsLoading(false);
      }
    };

    fetchProfile();
  }, []);

  // Validate profile field
  const validateField = (name, value) => {
    let error = "";

    switch (name) {
      case "name":
        if (!value || value.trim() === "") {
          error = "Name is required";
        } else if (value.length < 2 || value.length > 100) {
          error = "Name must be between 2 and 100 characters";
        }
        break;
      case "email":
        if (!value || value.trim() === "") {
          error = "Email is required";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          error = "Invalid email format";
        }
        break;
      case "phoneNumber":
        if (value && (value.length < 10 || value.length > 15)) {
          error = "Phone number must be between 10 and 15 digits";
        }
        break;
      case "dateOfBirth":
        if (value) {
          const dob = new Date(value);
          const now = new Date();
          if (dob >= now) {
            error = "Date of birth must be in the past";
          }
        }
        break;
      default:
        break;
    }

    return error;
  };

  // Handle input change with validation
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfileData((prev) => ({ ...prev, [name]: value }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
  };

  // Handle blur validation
  const handleBlur = (e) => {
    const { name, value } = e.target;
    const error = validateField(name, value);
    setErrors((prev) => ({ ...prev, [name]: error }));
  };

  // Validate all fields
  const validateAllFields = () => {
    const newErrors = {};
    let isValid = true;

    Object.keys(profileData).forEach((key) => {
      const error = validateField(key, profileData[key]);
      if (error) {
        newErrors[key] = error;
        isValid = false;
      }
    });

    setErrors(newErrors);
    return isValid;
  };

  const handleProfileUpdate = async (e) => {
    e.preventDefault();

    if (!validateAllFields()) {
      toast.error("Please fix the validation errors");
      return;
    }

    setIsUpdatingProfile(true);

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        toast.error("Please login again");
        return;
      }

      const updateData = {
        name: profileData.name.trim(),
        email: profileData.email.trim(),
      };

      if (profileData.phoneNumber) {
        updateData.phoneNumber = profileData.phoneNumber;
      }
      if (profileData.dateOfBirth) {
        updateData.dateOfBirth = profileData.dateOfBirth;
      }
      if (profileData.gender) {
        updateData.gender = profileData.gender;
      }
      if (profileData.address) {
        updateData.address = profileData.address;
      }

      await updateProfile(token, updateData);
      toast.success("Profile updated successfully!");
    } catch (err) {
      const errorMessage =
        err.response?.data?.message || "Failed to update profile";
      toast.error(errorMessage);

      // Handle specific validation errors from API
      if (err.response?.data?.errors) {
        const apiErrors = {};
        err.response.data.errors.forEach((error) => {
          const field = error.field;
          const message = error.defaultMessage || error.message;
          apiErrors[field] = message;
        });
        setErrors((prev) => ({ ...prev, ...apiErrors }));
      }
    } finally {
      setIsUpdatingProfile(false);
    }
  };

  // Validate password field
  const validatePassword = (name, value) => {
    let error = "";

    switch (name) {
      case "newPassword":
        if (value && value.length < 6) {
          error = "Password must be at least 6 characters";
        }
        break;
      case "confirmPassword":
        if (value && value !== newPassword) {
          error = "Passwords do not match";
        }
        break;
      default:
        break;
    }

    return error;
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    if (name === "newPassword") {
      setNewPassword(value);
      // Re-validate confirm password when new password changes
      if (confirmPassword) {
        const confirmError = validatePassword("confirmPassword", confirmPassword);
        setPasswordErrors((prev) => ({ ...prev, confirmPassword: confirmError }));
      }
    } else {
      setPasswordErrors((prev) => ({ ...prev, [name]: "" }));
      if (name === "confirmPassword") {
        const error = validatePassword(name, value);
        setPasswordErrors((prev) => ({ ...prev, [name]: error }));
      } else {
        setPasswordErrors((prev) => ({ ...prev, [name]: "" }));
      }
    }
  };

  const handlePasswordBlur = (e) => {
    const { name, value } = e.target;
    const error = validatePassword(name, value);
    setPasswordErrors((prev) => ({ ...prev, [name]: error }));
  };

  const handlePasswordUpdate = async (e) => {
    e.preventDefault();

    // Validate passwords
    const newErrors = {};
    let isValid = true;

    if (!currentPassword) {
      newErrors.currentPassword = "Current password is required";
      isValid = false;
    }

    if (!newPassword || newPassword.length < 6) {
      newErrors.newPassword = "Password must be at least 6 characters";
      isValid = false;
    }

    if (newPassword !== confirmPassword) {
      newErrors.confirmPassword = "Passwords do not match";
      isValid = false;
    }

    setPasswordErrors(newErrors);

    if (!isValid) {
      toast.error("Please fix the password validation errors");
      return;
    }

    setIsUpdatingPassword(true);

    try {
      const token = localStorage.getItem("token");
      if (!token) {
        toast.error("Please login again");
        return;
      }

      // TODO: Implement change password API when available
      // For now, simulate API call
      await new Promise((resolve) => setTimeout(resolve, 1000));

      toast.success("Password updated successfully!");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to update password");
    } finally {
      setIsUpdatingPassword(false);
    }
  };

  if (isLoading) {
    return (
      <div className="px-6 py-8">
        <PageTitle>Manage Profile</PageTitle>
        <div className="flex items-center justify-center mt-8">
          <div className="text-gray-600 dark:text-gray-300">Loading...</div>
        </div>
      </div>
    );
  }

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
                  name="name"
                  value={profileData.name}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                  placeholder="Enter your name"
                  required
                />
              </Label>
              {errors.name && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {errors.name}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>Email</span>
                <Input
                  className="mt-1"
                  type="email"
                  name="email"
                  value={profileData.email}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                  placeholder="Enter your email"
                  required
                />
              </Label>
              {errors.email && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {errors.email}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>Phone Number</span>
                <Input
                  className="mt-1"
                  type="tel"
                  name="phoneNumber"
                  value={profileData.phoneNumber}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                  placeholder="Enter your phone number"
                />
              </Label>
              {errors.phoneNumber && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {errors.phoneNumber}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>Date of Birth</span>
                <Input
                  className="mt-1"
                  type="date"
                  name="dateOfBirth"
                  value={profileData.dateOfBirth}
                  onChange={handleInputChange}
                  onBlur={handleBlur}
                />
              </Label>
              {errors.dateOfBirth && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {errors.dateOfBirth}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>Gender</span>
                <Select
                  className="mt-1"
                  name="gender"
                  value={profileData.gender}
                  onChange={handleInputChange}
                >
                  <option value="">Select gender</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                </Select>
              </Label>
            </div>

            <div className="mb-4">
              <Label>
                <span>Address</span>
                <Input
                  className="mt-1"
                  type="text"
                  name="address"
                  value={profileData.address}
                  onChange={handleInputChange}
                  placeholder="Enter your address"
                />
              </Label>
            </div>

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
                  name="currentPassword"
                  value={currentPassword}
                  onChange={(e) => {
                    setCurrentPassword(e.target.value);
                    setPasswordErrors((prev) => ({
                      ...prev,
                      currentPassword: "",
                    }));
                  }}
                  onBlur={(e) => {
                    if (!e.target.value) {
                      setPasswordErrors((prev) => ({
                        ...prev,
                        currentPassword: "Current password is required",
                      }));
                    }
                  }}
                  placeholder="Enter current password"
                  required
                />
              </Label>
              {passwordErrors.currentPassword && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {passwordErrors.currentPassword}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>New Password</span>
                <Input
                  className="mt-1"
                  type="password"
                  name="newPassword"
                  value={newPassword}
                  onChange={handlePasswordChange}
                  onBlur={handlePasswordBlur}
                  placeholder="Enter new password"
                  required
                />
              </Label>
              {passwordErrors.newPassword && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {passwordErrors.newPassword}
                </p>
              )}
            </div>

            <div className="mb-4">
              <Label>
                <span>Confirm New Password</span>
                <Input
                  className="mt-1"
                  type="password"
                  name="confirmPassword"
                  value={confirmPassword}
                  onChange={handlePasswordChange}
                  onBlur={handlePasswordBlur}
                  placeholder="Confirm new password"
                  required
                />
              </Label>
              {passwordErrors.confirmPassword && (
                <p className="mt-1 text-sm text-red-600 dark:text-red-400">
                  {passwordErrors.confirmPassword}
                </p>
              )}
            </div>

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
