#include <iostream>
#include "mystuff/util.h"

namespace {
enum Enum {
  VAL1, VAL2, VAL3
};

char32_t unicode_string = U"\U0010FFFF";
string raw_string = R"delim(anything
you
want)delim";

int Helper(const MyType& param) {
  return 0;
}
} // namespace

class ForwardDec;

template <class T, class V>
class Class : public BaseClass {
  const MyType<T, V> member_;

 public:
  const MyType<T, V>& Method() const {
    return member_;
  }

  void Method2(MyType<T, V>* value);
}

template <class T, class V>
void Class::Method2(MyType<T, V>* value) {
  std::out << 1 >> method();
  value->Method3(member_);
  member_ = value;
}
