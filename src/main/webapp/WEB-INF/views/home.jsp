<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html><html lang="ko"><head>
  <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Morrow — 오래 곁에 둘 물건</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/shop.css"><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog.css"><script defer src="${pageContext.request.contextPath}/assets/js/shop.js"></script>
</head><body>
  <header class="site-header"><a class="brand" href="${pageContext.request.contextPath}/">Morrow<span>.</span></a><nav aria-label="주요 메뉴"><a href="#catalog">Shop</a><a href="#story">Our story</a><a href="#journal">Journal</a></nav><div class="header-actions"><c:choose><c:when test="${not empty sessionScope.loginUser}"><a href="${pageContext.request.contextPath}/account/profile"><c:out value="${sessionScope.loginUser.name}"/>님</a></c:when><c:otherwise><a href="${pageContext.request.contextPath}/login">Sign in</a></c:otherwise></c:choose><button class="bag" type="button" aria-label="장바구니 열기">Bag <span id="bag-count">0</span></button></div></header>
  <main>
    <section class="hero"><div class="hero-copy"><p class="eyebrow">Objects for slower days</p><h1>오늘보다 조금 더<br><em>다정한 내일.</em></h1><p class="lead">유행보다 오래 남는 형태와 쓰임을 골랐습니다. 매일의 작은 장면을 바꾸는 물건을 만나보세요.</p><a class="primary-button" href="#catalog">컬렉션 보기 <span>↘</span></a></div><div class="hero-visual" aria-label="자연광이 드는 생활 공간"><div class="hero-note"><span>01</span><p>Quiet forms<br>for daily rituals</p></div></div></section>
    <section class="catalog" id="catalog">
      <div class="section-heading"><div><p class="eyebrow">The edit</p><h2>이번 주의 물건</h2></div><p>형태와 소재, 만드는 태도를 기준으로<br>천천히 고른 생활의 도구입니다.</p></div>
      <form class="catalog-filter" method="get" action="${pageContext.request.contextPath}/products" role="search">
        <label class="search-field"><span class="sr-only">상품 검색</span><input name="keyword" value="<c:out value='${keyword}'/>" maxlength="100" placeholder="어떤 물건을 찾고 있나요?"><button type="submit" aria-label="검색">↗</button></label>
        <div class="category-filter" aria-label="카테고리 필터"><a class="${empty selectedCategory ? 'active' : ''}" href="${pageContext.request.contextPath}/products">All</a><c:forEach var="item" items="${categories}"><button class="${selectedCategory == item ? 'active' : ''}" name="category" value="<c:out value='${item}'/>" type="submit"><c:out value="${item}"/></button></c:forEach></div>
      </form>
      <c:if test="${not empty catalogError}"><div class="notice error"><c:out value="${catalogError}"/></div></c:if>
      <div class="product-grid"><c:forEach var="product" items="${products}" varStatus="status"><article class="product-card ${status.index == 0 ? 'product-card--wide' : ''}"><a class="product-image" href="${pageContext.request.contextPath}/product?id=${product.id}" aria-label="${product.name} 상세 보기"><img src="<c:out value='${product.imageUrl}'/>" alt="<c:out value='${product.name}'/>" loading="lazy"><c:if test="${product.featured}"><span class="badge">Morrow pick</span></c:if><span class="view-arrow" aria-hidden="true">↗</span></a><div class="product-info"><div><p class="category"><c:out value="${product.category}"/></p><h3><c:out value="${product.name}"/></h3></div><p class="price"><fmt:formatNumber value="${product.price}" pattern="#,##0"/>원</p></div></article></c:forEach></div>
      <c:if test="${empty products and empty catalogError}"><div class="notice"><strong>조건에 맞는 상품이 없습니다.</strong><br>검색어를 바꾸거나 전체 카테고리를 둘러보세요.</div></c:if>
    </section>
    <section class="manifesto" id="story"><p>We keep what matters.</p><h2>잘 만든 물건은 일상을<br>조용히 바꾼다고 믿어요.</h2><a href="#">Morrow의 기준 읽기 →</a></section>
  </main><footer><a class="brand" href="#">Morrow<span>.</span></a><p>Seoul · Everyday objects · © 2026</p></footer>
</body></html>
