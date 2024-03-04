chmod +x ./gradlew

cd ./app

echo "GooglePlay AAB build"
../gradlew bundleGoogleplayRelease

echo "RuStore AAB build"
../gradlew bundleRustoreRelease

mkdir -p ../build/bundles
find ./build/outputs/bundle -name "*.aab" | xargs -I % cp % ../build/bundles
rm -rf ./build
