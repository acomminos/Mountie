// Macros to translate GNU endian conversion calls to Android/BSD equivalents.
// Observe that defining _BSD_SOURCE in cryptsetup does nothing to fix this.

#define be16toh(x) betoh16(x)
#define be32toh(x) betoh32(x)
#define be64toh(x) betoh64(x)
#define le16toh(x) letoh16(x)
#define le32toh(x) letoh32(x)
#define le64toh(x) letoh64(x)