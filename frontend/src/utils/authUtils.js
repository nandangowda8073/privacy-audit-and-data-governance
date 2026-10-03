export const hasPermission = (user, permission) => {
    if (!user || !user.permissions) return false;
    return user.permissions.includes(permission);
};

export const hasRole = (user, role) => {
    if (!user || !user.role) return false;
    return user.role === role || user.role === `ROLE_${role}`;
};

export const hasAnyPermission = (user, permissions = []) => {
    if (!user || !user.permissions) return false;
    return permissions.some(p => user.permissions.includes(p));
};
